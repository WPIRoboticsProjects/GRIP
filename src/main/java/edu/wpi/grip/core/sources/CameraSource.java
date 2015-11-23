package edu.wpi.grip.core.sources;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.FatalErrorEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
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

    private final SocketHint<Mat> imageOutputHint = new SocketHint<Mat>("Image", Mat.class, Mat::new);
    private final SocketHint<Number> frameRateOutputHint = new SocketHint<Number>("Frame Rate", Number.class, 0);
    private OutputSocket<Mat> frameOutputSocket;
    private OutputSocket<Number> frameRateOutputSocket;
    private Optional<Thread> frameThread;
    private Optional<FrameGrabber> grabber;

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
        this.grabber = Optional.empty();
        this.frameThread = Optional.empty();
    }

    private void initialize(EventBus eventBus, FrameGrabber frameGrabber, String name) throws IOException {
        this.eventBus = checkNotNull(eventBus, "Event Bus was null.");
        this.name = name;
        this.frameOutputSocket = new OutputSocket<>(eventBus, imageOutputHint);
        this.frameRateOutputSocket = new OutputSocket<>(eventBus, frameRateOutputHint);

        this.eventBus.register(this);
        this.startVideo(frameGrabber);
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
        final String deviceNumber = properties.getProperty(DEVICE_NUMBER_PROPERTY);
        final String address = properties.getProperty(ADDRESS_PROPERTY);

        if (deviceNumber != null) {
            this.initialize(eventBus, new OpenCVFrameGrabber(Integer.valueOf(deviceNumber)), "Webcam " + deviceNumber);
        } else if (address != null) {
            this.initialize(eventBus, new IPCameraFrameGrabber(address), "IP Camera " + new URL(address).getHost());
        } else {
            throw new IllegalArgumentException("Cannot initialize CameraSource without either a device number or " +
                    "address");
        }
    }

    /**
     * Starts the video capture from the
     *
     * @param grabber A JavaCV {@link FrameGrabber} instance to capture from
     */
    private synchronized void startVideo(FrameGrabber grabber) throws IOException {
        final OpenCVFrameConverter.ToMat convertToMat = new OpenCVFrameConverter.ToMat();
        if (this.frameThread.isPresent()) {
            throw new IllegalStateException("The video retrieval thread has already been started.");
        }
        if (this.grabber.isPresent()) {
            throw new IllegalStateException("The Frame Grabber has already been started.");
        }
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            throw new IOException("A problem occurred trying to start the frame grabber for " + this.name, e);
        }

        // Store the grabber only once it has been started in the case that there is an exception.
        this.grabber = Optional.of(grabber);

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

                frameMat.copyTo(frameOutputSocket.getValue());
                frameOutputSocket.setValue(frameOutputSocket.getValue());
                long thisMoment = System.currentTimeMillis();
                frameRateOutputSocket.setValue(1000 / (thisMoment - lastFrame));
                lastFrame = thisMoment;
            }
        });
        frameExecutor.setUncaughtExceptionHandler(
                (thread, exception) -> {
                    // TODO Pass Exception to the UI.
                    System.err.println("Webcam Frame Grabber Thread crashed with uncaught exception:");
                    exception.printStackTrace();
                    eventBus.post(new FatalErrorEvent(exception));
                    try {
                        stopVideo();
                    } catch (TimeoutException e) {
                        System.err.println("Webcam Frame Grabber could not be stopped!");
                        e.printStackTrace();
                        eventBus.post(new FatalErrorEvent(e));
                    }
                }
        );
        frameExecutor.setDaemon(true);
        frameExecutor.start();
        frameThread = Optional.of(frameExecutor);
    }

    /**
     * Stops the video feed from updating the output socket.
     *
     * @throws TimeoutException If the thread running the Webcam fails to join this one after a timeout.
     */
    private void stopVideo() throws TimeoutException {
        if (frameThread.isPresent()) {
            final Thread ex = frameThread.get();
            ex.interrupt();
            try {
                ex.join(TimeUnit.SECONDS.toMillis(2));
                if (ex.isAlive()) {
                    throw new TimeoutException("Unable to terminate video feed from Web Camera");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                //TODO: Move this into a logging framework
                System.out.println("Caught Exception:");
                e.printStackTrace();
            } finally {
                frameThread = Optional.empty();
                // This will always run even if a timeout exception occurs
                try {
                    grabber.ifPresent((openCVFrameGrabber) -> {
                        try {
                            openCVFrameGrabber.stop();
                        } catch (FrameGrabber.Exception e) {
                            throw new IllegalStateException("A problem occurred trying to stop the frame grabber", e);
                        }
                    });
                } finally {
                    // This will always run even if we fail to stop the grabber
                    grabber = Optional.empty();
                }
            }
        } else {
            throw new IllegalStateException("Tried to stop a Webcam that is already stopped.");
        }
    }

    @Subscribe
    public void onSourceRemovedEvent(SourceRemovedEvent event) throws TimeoutException {
        if (event.getSource() == this) {
            this.stopVideo();
            this.eventBus.unregister(this);
        }
    }

}
