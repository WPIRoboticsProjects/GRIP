package edu.wpi.grip.core.sources;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.Source;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebcamSource implements Source {
    private final SocketHint<Mat> imageOutputHint = new SocketHint<Mat>("Image", Mat.class, Mat::new);
    private final OutputSocket<Mat> outputSocket;
    final private ScheduledThreadPoolExecutor executor;
    private OpenCVFrameGrabber grabber;
    private final int FRAME_RATE = 30;

    public WebcamSource(EventBus eventBus) {
        checkNotNull(eventBus, "Event Bus was null.");
        outputSocket = new OutputSocket(eventBus, imageOutputHint);
        executor = new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public OutputSocket[] getOutputSockets() {
        return new OutputSocket[]{outputSocket};
    }

    public void startVideo(final int deviceNumber) {
        OpenCVFrameConverter.ToMat convertToMat = new OpenCVFrameConverter.ToMat();
        final int captureWidth = 1280;
        final int captureHeight = 720;
        grabber = new OpenCVFrameGrabber(deviceNumber);
        grabber.setImageWidth(captureWidth);
        grabber.setImageHeight(captureHeight);


        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            throw new IllegalStateException("A problem occurred trying to start the frame grabber", e);
        }

        executor.scheduleAtFixedRate(() -> {
                    final Frame videoFrame;
                    try {
                        videoFrame = grabber.grab();
                    } catch (FrameGrabber.Exception e) {
                        throw new IllegalStateException("Failed to grab image", e);
                    }
                    outputSocket.setValue(convertToMat.convert(videoFrame));
                },
                0, (long) 1000 / FRAME_RATE, TimeUnit.MILLISECONDS
        );
    }

    public void stopVideo() throws TimeoutException {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                // Try again a little more forcefully
                executor.shutdownNow();
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    throw new TimeoutException("Unable to terminate video feed from Web Camera");
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Caught Exception:");
            e.printStackTrace();
        } finally {
            // This will always run even if a timeout exception occurs
            try {
                grabber.stop();
            } catch (FrameGrabber.Exception e) {
                throw new IllegalStateException("A problem occurred trying to stop the frame grabber", e);
            }
        }

    }

}
