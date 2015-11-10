package edu.wpi.grip.core.sources;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides a way to generate a constantly updated {@link Mat} from a Webcam attached
 * to the user's computer.
 */
public class WebcamSource implements Source {

    private final String name;
    private final EventBus eventBus;
    private final SocketHint<Mat> imageOutputHint = new SocketHint<Mat>("Image", Mat.class, Mat::new);
    private final SocketHint<Number> frameRateOutputHint = new SocketHint<Number>("Frame Rate", Number.class, 0);
    private OutputSocket<Mat> frameOutputSocket;
    private OutputSocket<Number> frameRateOutputSocket;
    private Optional<Thread> frameThread;
    private Optional<OpenCVFrameGrabber> grabber;

    /**
     * Creates a Webcam source that can be used as an input to a pipeline
     *
     * @param eventBus The EventBus to attach to
     */
    public WebcamSource(EventBus eventBus, int deviceNumber) {
        checkNotNull(eventBus, "Event Bus was null.");
        this.eventBus = eventBus;

        this.name = "Webcam " + deviceNumber;

        this.frameOutputSocket = new OutputSocket(eventBus, imageOutputHint);
        this.frameRateOutputSocket = new OutputSocket(eventBus, frameRateOutputHint);
        this.grabber = Optional.empty();
        this.frameThread = Optional.empty();

        this.startVideo(deviceNumber);
        eventBus.register(this);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public OutputSocket[] getOutputSockets() {
        return new OutputSocket[]{frameOutputSocket, frameRateOutputSocket};
    }

    /**
     * Starts the video capture from the
     *
     * @param deviceNumber The index of the Webcam device that should be attached to
     */
    private synchronized void startVideo(final int deviceNumber) {
        final OpenCVFrameConverter.ToMat convertToMat = new OpenCVFrameConverter.ToMat();
        final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(deviceNumber);
        if (this.frameThread.isPresent()) {
            throw new IllegalStateException("The video retrieval thread has already been started.");
        }
        if (this.grabber.isPresent()) {
            throw new IllegalStateException("The Frame Grabber has already been started.");
        }
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            throw new IllegalStateException("A problem occurred trying to start the frame grabber", e);
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

                frameOutputSocket.setValue(frameMat);
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
                    try {
                        stopVideo();
                    } catch (TimeoutException e) {
                        System.err.println("Webcam Frame Grabber could not be stopped!");
                        e.printStackTrace();
                    }
                }
        );
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
