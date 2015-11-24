package edu.wpi.grip.core.sources;


import com.google.common.base.StandardSystemProperty;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.SocketHints;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.events.SourceStartedEvent;
import edu.wpi.grip.core.events.SourceStoppedEvent;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.*;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides a way to generate a constantly updated {@link Mat} from a camera
 */
@XStreamAlias(value = "grip:Camera")
public class CameraSource extends Source {

    private final static String DEVICE_NUMBER_PROPERTY = "deviceNumber";
    private final static String ADDRESS_PROPERTY = "address";

    private EventBus eventBus;
    private String name;

    private Properties properties = new Properties();

    private final SocketHint<Mat> imageOutputHint = SocketHints.Inputs.createMatSocketHint("Image", true);
    private final SocketHint<Number> frameRateOutputHint = SocketHints.createNumberSocketHint("Frame Rate", 0);
    private OutputSocket<Mat> frameOutputSocket;
    private OutputSocket<Number> frameRateOutputSocket;
    private Optional<Thread> frameThread;
    private FrameGrabber grabber;

    /**
     * Creates a camera source that can be used as an input to a pipeline
     *
     * @param eventBus     The EventBus to attach to
     * @param deviceNumber The device number of the webcam
     */
    public CameraSource(EventBus eventBus, int deviceNumber) throws IOException {
        this();
        this.properties.setProperty(DEVICE_NUMBER_PROPERTY, "" + deviceNumber);
        this.createFromProperties(eventBus, this.properties);
    }

    /**
     * Creates a camera source that can be used as an input to a pipeline
     *
     * @param eventBus The EventBus to attach to
     * @param address  A URL to stream video from an IP camera
     */
    public CameraSource(EventBus eventBus, String address) throws IOException {
        this();
        this.properties.setProperty(ADDRESS_PROPERTY, address);
        this.createFromProperties(eventBus, this.properties);
    }

    /**
     * Used for serialization
     */
    public CameraSource() {
        this.frameThread = Optional.empty();
    }

    private void initialize(EventBus eventBus, FrameGrabber frameGrabber, String name) throws IOException {
        this.eventBus = checkNotNull(eventBus, "Event Bus was null.");
        this.name = name;
        this.frameOutputSocket = new OutputSocket<>(eventBus, imageOutputHint);
        this.frameRateOutputSocket = new OutputSocket<>(eventBus, frameRateOutputHint);
        this.grabber = frameGrabber;
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
    public void createFromProperties(EventBus eventBus, Properties properties) throws IOException {
        this.properties = properties;

        final String deviceNumberProperty = properties.getProperty(DEVICE_NUMBER_PROPERTY);
        final String addressProperty = properties.getProperty(ADDRESS_PROPERTY);

        if (deviceNumberProperty != null) {
            final int deviceNumber = Integer.valueOf(deviceNumberProperty);

            // On Windows, videoInput is much more reliable for webcam capture.  On other platforms, OpenCV's frame
            // grabber class works fine.
            if (StandardSystemProperty.OS_NAME.value().contains("Windows")) {
                this.initialize(eventBus, new VideoInputFrameGrabber(deviceNumber), "Webcam " + deviceNumber);
            } else {
                this.initialize(eventBus, new OpenCVFrameGrabber(deviceNumber), "Webcam " + deviceNumber);
            }
        } else if (addressProperty != null) {
            this.initialize(eventBus, new IPCameraFrameGrabber(addressProperty), "IP Camera " + new URL(addressProperty).getHost());
        } else {
            throw new IllegalArgumentException("Cannot initialize CameraSource without either a device number or " +
                    "address");
        }
    }

    /**
     * Starts the video capture from the source device
     */
    public CameraSource start() throws IOException, IllegalStateException {
        final OpenCVFrameConverter.ToMat convertToMat = new OpenCVFrameConverter.ToMat();
        synchronized (this.frameThread) {
            if (this.frameThread.isPresent()) {
                throw new IllegalStateException("The video retrieval thread has already been started.");
            }
            try {
                grabber.start();
            } catch (FrameGrabber.Exception e) {
                throw new IOException("A problem occurred trying to start the frame grabber for " + this.name, e);
            }

            final Thread frameExecutor = new Thread(() -> {
                long lastFrame = System.currentTimeMillis();
                while (!Thread.interrupted()) {
                    final Frame videoFrame;
                    try {
                        videoFrame = grabber.grab();
                    } catch (FrameGrabber.Exception e) {
                        throw new IllegalStateException("Failed to grab image", e);
                    }

                    final Mat frameMat = convertToMat.convert(videoFrame);

                    if (frameMat == null || frameMat.isNull()) {
                        throw new IllegalStateException("The camera returned a null frame Mat");
                    }

                    frameMat.copyTo(frameOutputSocket.getValue().get());
                    frameOutputSocket.setValue(frameOutputSocket.getValue().get());
                    long thisMoment = System.currentTimeMillis();
                    frameRateOutputSocket.setValue(1000 / (thisMoment - lastFrame));
                    lastFrame = thisMoment;
                }
            }, "Camera");

            frameExecutor.setUncaughtExceptionHandler(
                    (thread, exception) -> {
                        eventBus.post(new UnexpectedThrowableEvent(exception, "Camera Frame Grabber Thread crashed with uncaught exception"));
                        try {
                            stop();
                        } catch (TimeoutException e) {
                            eventBus.post(new UnexpectedThrowableEvent(e, "Camera Frame Grabber could not be stopped!"));
                        }
                    }
            );
            frameExecutor.setDaemon(true);
            frameExecutor.start();
            this.frameThread = Optional.of(frameExecutor);
        }
        eventBus.post(new SourceStartedEvent(this));
        return this;
    }

    /**
     * Stops the video feed from updating the output socket.
     *
     * @throws TimeoutException      If the thread running the Webcam fails to join this one after a timeout.
     * @throws IllegalStateException If the camera was already stopped
     */
    public CameraSource stop() throws TimeoutException, IllegalStateException {
        synchronized (this.frameThread) {
            if (frameThread.isPresent()) {
                final Thread ex = frameThread.get();
                ex.interrupt();
                try {
                    ex.join(TimeUnit.SECONDS.toMillis(500));
                    if (ex.isAlive()) {
                        throw new TimeoutException("Unable to terminate video feed from Web Camera");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    //TODO: Move this into a logging framework
                    System.out.println("Caught Exception:");
                    e.printStackTrace();
                } finally {
                    // Clean up this resource as you can't restart a stopped thread
                    this.frameThread = Optional.empty();
                    // This will always run even if a timeout exception occurs
                    try {
                        grabber.stop();
                    } catch (FrameGrabber.Exception e) {
                        throw new IllegalStateException("A problem occurred trying to stop the frame grabber", e);
                    }
                }
            } else {
                throw new IllegalStateException("Tried to stop a Webcam that is already stopped.");
            }
        }
        eventBus.post(new SourceStoppedEvent(this));
        frameRateOutputSocket.setValue(0);
        return this;
    }

    @Override
    public boolean isRunning() {
        synchronized (this.frameThread) {
            return this.frameThread.isPresent() && this.frameThread.get().isAlive();
        }
    }

    @Override
    public boolean isRestartable() {
        return true;
    }

    @Subscribe
    public void onSourceRemovedEvent(SourceRemovedEvent event) throws TimeoutException {
        if (event.getSource() == this) {
            try {
                if (this.isRunning()) this.stop();
            } finally {
                this.eventBus.unregister(this);
            }
        }
    }

}
