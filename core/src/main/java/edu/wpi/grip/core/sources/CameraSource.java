package edu.wpi.grip.core.sources;


import com.google.common.base.StandardSystemProperty;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.events.StartedStoppedEvent;
import edu.wpi.grip.core.events.StopPipelineEvent;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.util.ExceptionWitness;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a way to generate a constantly updated {@link Mat} from a camera
 */
@XStreamAlias(value = "grip:Camera")
public class CameraSource extends Source implements StartStoppable {

    /**
     * The path that Axis cameras stream MJPEG videos from.  Although any URL can be supplied
     * {@link CameraSource.Factory#create(String)}, allowing this to work with basically any network video stream, this
     * default path allows the Axis M1011 cameras used in FRC to work when only an IP address is supplied.
     */
    public final static String DEFAULT_IP_CAMERA_PATH = "/mjpg/video.mjpg";

    private final static String DEVICE_NUMBER_PROPERTY = "deviceNumber";
    private final static String ADDRESS_PROPERTY = "address";
    private static Logger logger = Logger.getLogger(CameraSource.class.getName());

    private final EventBus eventBus;
    private final String name;

    private final Properties properties;

    private final SocketHint<Mat> imageOutputHint = SocketHints.Inputs.createMatSocketHint("Image", true);
    private final SocketHint<Number> frameRateOutputHint = SocketHints.createNumberSocketHint("Frame Rate", 0);
    private final OutputSocket<Mat> frameOutputSocket;
    private final OutputSocket<Number> frameRateOutputSocket;
    private final FrameGrabber grabber;
    private Optional<Thread> frameThread;


    public interface Factory {
        CameraSource create(int deviceNumber) throws IOException;

        CameraSource create(String address) throws IOException;

        CameraSource create(Properties properties) throws IOException;
    }

    public interface FrameGrabberFactory {
        FrameGrabber create(int deviceNumber);

        FrameGrabber create(String addressProperty) throws MalformedURLException;
    }

    public static class FrameGrabberFactoryImpl implements FrameGrabberFactory {
        FrameGrabberFactoryImpl() { /* no-op */ }

        public FrameGrabber create(int deviceNumber) {
            // On Windows, videoInput is much more reliable for webcam capture.  On other platforms, OpenCV's frame
            // grabber class works fine.
            if (StandardSystemProperty.OS_NAME.value().contains("Windows")) {
                return new VideoInputFrameGrabber(deviceNumber);
            } else {
                return new OpenCVFrameGrabber(deviceNumber);
            }
        }

        public FrameGrabber create(String addressProperty) throws MalformedURLException {
            // If no path was specified in the URL (ie: it was something like http://10.1.90.11/), use the default path
            // for Axis M1011 cameras.
            if (new URL(addressProperty).getPath().length() <= 1) {
                addressProperty += DEFAULT_IP_CAMERA_PATH;
            }
            return new IPCameraFrameGrabber(addressProperty);
        }
    }

    /**
     * Creates a camera source that can be used as an input to a pipeline
     *
     * @param eventBus     The EventBus to attach to
     * @param deviceNumber The device number of the webcam
     */
    @AssistedInject
    CameraSource(
            final EventBus eventBus,
            final FrameGrabberFactory grabberFactory,
            final ExceptionWitness.Factory exceptionWitnessFactory,
            @Assisted final int deviceNumber) throws IOException {
        this(eventBus, grabberFactory, exceptionWitnessFactory, createProperties(deviceNumber));
    }

    /**
     * Creates a camera source that can be used as an input to a pipeline
     *
     * @param eventBus The EventBus to attach to
     * @param address  A URL to stream video from an IP camera
     */
    @AssistedInject
    CameraSource(
            final EventBus eventBus,
            final FrameGrabberFactory grabberFactory,
            final ExceptionWitness.Factory exceptionWitnessFactory,
            @Assisted final String address) throws IOException {
        this(eventBus, grabberFactory, exceptionWitnessFactory, createProperties(address));
    }

    /**
     * Used for serialization
     */
    @AssistedInject
    CameraSource(
            final EventBus eventBus,
            final FrameGrabberFactory grabberFactory,
            final ExceptionWitness.Factory exceptionWitnessFactory,
            @Assisted final Properties properties) throws MalformedURLException {
        super(exceptionWitnessFactory);
        this.frameThread = Optional.empty();
        this.eventBus = eventBus;
        this.frameOutputSocket = new OutputSocket<>(eventBus, imageOutputHint);
        this.frameRateOutputSocket = new OutputSocket<>(eventBus, frameRateOutputHint);
        this.properties = properties;

        final String deviceNumberProperty = properties.getProperty(DEVICE_NUMBER_PROPERTY);
        final String addressProperty = properties.getProperty(ADDRESS_PROPERTY);

        if (deviceNumberProperty != null) {
            final int deviceNumber = Integer.valueOf(deviceNumberProperty);
            this.name = "Webcam " + deviceNumber;
            this.grabber = grabberFactory.create(deviceNumber);
        } else if (addressProperty != null) {
            this.name = "IP Camera " + new URL(addressProperty).getHost();
            this.grabber = grabberFactory.create(addressProperty);
        } else {
            throw new IllegalArgumentException("Cannot initialize CameraSource without either a device number or " +
                    "address");
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public OutputSocket[] createOutputSockets() {
        return new OutputSocket[]{frameOutputSocket, frameRateOutputSocket};
    }

    @Override
    public Properties getProperties() {
        return this.properties;
    }

    @Override
    public void initialize() throws IOException {
        start();
    }

    /**
     * Starts the video capture from this frame grabber.
     */
    public void start() throws IOException, IllegalStateException {
        final OpenCVFrameConverter.ToMat convertToMat = new OpenCVFrameConverter.ToMat();
        synchronized (this) {
            if (this.frameThread.isPresent()) {
                throw new IllegalStateException("The video retrieval thread has already been started.");
            }
            try {
                // If the thread shutdown because of an exception the grabber may still be running
                // This will allow us to make sure that everything is cleaned up correctly.
                grabber.restart();
            } catch (FrameGrabber.Exception e) {
                throw new IOException("A problem occurred trying to start the frame grabber for " + this.name, e);
            }

            final Thread frameExecutor = new Thread(() -> {
                try {
                    long lastFrame = System.nanoTime();
                    while (!Thread.currentThread().isInterrupted()) {
                        final Frame videoFrame;
                        try {
                            videoFrame = grabber.grab();
                        } catch (FrameGrabber.Exception e) {
                            throw new IllegalStateException("Failed to grab image", e);
                        }

                        final Mat frameMat = convertToMat.convert(videoFrame);

                        if (frameMat == null || frameMat.isNull()) {
                            getExceptionWitness().flagWarning("The camera returned a null frame Mat");
                            continue; // Do not update the camera frame.
                        }

                        frameMat.copyTo(frameOutputSocket.getValue().get());
                        frameOutputSocket.setValue(frameOutputSocket.getValue().get());

                        final long thisMoment = System.nanoTime();
                        final long elapsedTime = thisMoment - lastFrame;
                        if (elapsedTime != 0) frameRateOutputSocket.setValue(1e9 / elapsedTime);
                        lastFrame = thisMoment;
                        getExceptionWitness().clearException();
                    }
                } finally {
                    // Calling frameGrabber.stop here will deadlock the program.
                    synchronized (this) {
                        // This has to be synchronized or both threads could be modifying it at the same time.
                        this.frameThread = Optional.empty();
                    }
                    // If this thread was interrupted than exit without doing this cleanup step
                    if (!Thread.currentThread().isInterrupted()) {
                        eventBus.post(new StartedStoppedEvent(this));
                        frameRateOutputSocket.setValue(0);
                    }
                }
            }, "Camera");

            frameExecutor.setUncaughtExceptionHandler(
                    (thread, exception) -> {
                        final String exceptionMessage = this.name + " Frame Grabber Thread crashed with uncaught exception";
                        // The FrameGrabber also uses an exception class named "Exception" so this is clearer
                        if (exception instanceof java.lang.Exception) {
                            getExceptionWitness().flagException((java.lang.Exception) exception, exceptionMessage);
                        } else {
                            eventBus.post(new UnexpectedThrowableEvent(exception, exceptionMessage));
                        }
                    }
            );
            frameExecutor.setDaemon(true);
            // This should happen before start is called in case
            // the frameThread crashes immediately and removes itself.
            this.frameThread = Optional.of(frameExecutor);
            frameExecutor.start();
        }
        // This should only be posted now that it is running
        eventBus.post(new StartedStoppedEvent(this));
    }

    /**
     * Stops this source.
     * This will stop the source publishing new socket values after this method returns.
     *
     * @return The source that was stopped
     * @throws TimeoutException      If the thread running the source fails to stop.
     * @throws IOException           If there is a problem stopping the Source
     * @throws IllegalStateException If the camera is already stopped.
     */
    public void stop() throws InterruptedException, TimeoutException, IOException {
        synchronized (this) {
            if (frameThread.isPresent()) {
                final Thread ex = frameThread.get();
                ex.interrupt();
                try {
                    for (int i = 0; i < 1000 && ex.isAlive(); i++) {
                        // We have to wait for the frame thread to be removed.
                        // This is done in a synchronized block in the finally block
                        wait(10);
                        ex.join(10);
                    }
                    // The frame thread should be removed at this point
                    if (ex.isAlive()) {
                        throw new TimeoutException("Unable to terminate video feed from " + this.name);
                    }
                    // The thread being interrupted should handle its own death by setting the frameThread to empty
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.log(Level.WARNING, e.getMessage(), e);
                    throw e;
                } finally {
                    // This will always run even if a timeout exception occurs
                    try {
                        // Calling this multiple times will have no effect
                        grabber.stop();
                    } catch (FrameGrabber.Exception e) {
                        throw new IOException("A problem occurred trying to stop the frame grabber for " + this.name, e);
                    }
                }
            } else {
                throw new IllegalStateException("Tried to stop " + this.name + " but it is already stopped.");
            }
        }
        eventBus.post(new StartedStoppedEvent(this));
        frameRateOutputSocket.setValue(0);
    }

    @Override
    public synchronized boolean isStarted() {
        return this.frameThread.isPresent() && this.frameThread.get().isAlive();
    }

    @Subscribe
    public void onSourceRemovedEvent(SourceRemovedEvent event) throws InterruptedException, TimeoutException, IOException {
        if (event.getSource() == this) {
            try {
                if (this.isStarted()) this.stop();
            } finally {
                this.eventBus.unregister(this);
            }
        }
    }

    private static Properties createProperties(String address) {
        final Properties properties = new Properties();
        properties.setProperty(ADDRESS_PROPERTY, address);
        return properties;
    }

    private static Properties createProperties(int deviceNumber) {
        final Properties properties = new Properties();
        properties.setProperty(DEVICE_NUMBER_PROPERTY, Integer.toString(deviceNumber));
        return properties;
    }

    /**
     * This stops the camera when a "StopPipelineEvent" is encountered.
     * @param event
     */
    @Subscribe
    public synchronized void onStopPipeline(StopPipelineEvent event){
        try {
            this.stop();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } catch (TimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

}
