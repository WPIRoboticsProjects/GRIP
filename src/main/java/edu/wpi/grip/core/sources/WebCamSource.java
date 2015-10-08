package edu.wpi.grip.core.sources;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.Source;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_videoio;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebCamSource implements Source {

    public enum SourceType {
        ANY(opencv_videoio.CV_CAP_ANY, "AutoDetect"),

        MIL(opencv_videoio.CV_CAP_MIL, "MIL proprietary drivers"),

        VFW(opencv_videoio.CV_CAP_VFW, "Platform Native"),
        V4L(opencv_videoio.CV_CAP_V4L, "Platform Native"),
        V4L2(opencv_videoio.CV_CAP_V4L2, "Platform Native"),

        FIREWARE(opencv_videoio.CV_CAP_FIREWARE, "IEEE 1394 drivers"),
        FIREWIRE(opencv_videoio.CV_CAP_FIREWIRE, "IEEE 1394 drivers"),
        IEEE1394(opencv_videoio.CV_CAP_IEEE1394, "IEEE 1394 drivers"),
        DC1394(opencv_videoio.CV_CAP_DC1394, "IEEE 1394 drivers"),
        CMU1394(opencv_videoio.CV_CAP_CMU1394, "IEEE 1394 drivers"),

        STEREO(opencv_videoio.CV_CAP_STEREO, "TYZX proprietary drivers"),
        TYZX(opencv_videoio.CV_CAP_TYZX , "TYZX proprietary drivers"),
        TYZX_LEFT(opencv_videoio.CV_TYZX_LEFT, "TYZX proprietary drivers"),
        TYZX_RIGHT(opencv_videoio.CV_TYZX_RIGHT, "TYZX proprietary drivers"),
        TYZX_COLOR(opencv_videoio.CV_TYZX_COLOR, "TYZX proprietary drivers"),
        TYZX_Z(opencv_videoio.CV_TYZX_Z, "TYZX proprietary drivers"),

        QT(opencv_videoio.CV_CAP_QT, "QuickTime"),

        UNICAP(opencv_videoio.CV_CAP_UNICAP, "Unicap drivers"),

        DSHOW(opencv_videoio.CV_CAP_DSHOW, "DirectShow (via videoInput)"),

        MSMF(opencv_videoio.CV_CAP_MSMF, "Microsoft Media Foundation (via videoInput)"),

        PVAPI(opencv_videoio.CV_CAP_PVAPI, "PvAPI, Prosilica GigE SDK"),

        OPENNI(opencv_videoio.CV_CAP_OPENNI, "OpenNI (for Kinect)"),

        OPENNI_ASUS(opencv_videoio.CV_CAP_OPENNI_ASUS, "OpenNI (for Asus Xtion)"),

        ANDROID(opencv_videoio.CV_CAP_ANDROID, "Android - not used"),
        ANDROID_BACK(opencv_videoio.CV_CAP_ANDROID_BACK, "Android back camera - not used"),
        ANDROID_FRONT(opencv_videoio.CV_CAP_ANDROID_FRONT, "Android front camera - not used"),

        XIAPI(opencv_videoio.CV_CAP_XIAPI, "XIMEA Camera API"),

        AVFOUNDATION(opencv_videoio.CV_CAP_AVFOUNDATION, "AVFoundation framework for iOS (OS X Lion will have the same API)"),

        GIGANETIX(opencv_videoio.CV_CAP_GIGANETIX, "Smartek Giganetix GigEVisionSDK"),

        INTELPERC(opencv_videoio.CV_CAP_INTELPERC, "Intel Perceptual Computing"),

        OPENNI2(opencv_videoio.CV_CAP_OPENNI2, "OpenNI2 (for Kinect)"),

        GPHOTO2(opencv_videoio.CV_CAP_GPHOTO2, "");

        final int value;
        final String group;
        SourceType(final int value, final String group){
            this.value = value;
            this.group = group;
        }
    }

    private final SocketHint<Mat> imageOutputHint = new SocketHint<Mat>("Image", Mat.class, Mat::new);
    private final SocketHint<Number> framerateOutputHint = new SocketHint<Number>("Framerate", Number.class, 0);
    private OutputSocket<Mat> frameOutputSocket;
    private OutputSocket<Number> frameRateOutputSocket;
    private Optional<Thread> executor;
    private Optional<OpenCVFrameGrabber> grabber;

    public WebCamSource(EventBus eventBus) {
        checkNotNull(eventBus, "Event Bus was null.");
        frameOutputSocket = new OutputSocket(eventBus, imageOutputHint);
        frameRateOutputSocket = new OutputSocket(eventBus, framerateOutputHint);
        grabber = Optional.empty();
        executor = Optional.empty();
    }

    @Override
    public OutputSocket[] getOutputSockets() {
        return new OutputSocket[]{frameOutputSocket, frameRateOutputSocket};
    }

    /**
     * Starts the video capture from the
     * @param deviceType The type of the device the webcam should be attached with
     */
    public synchronized void startVideo(final SourceType deviceType) {
        final OpenCVFrameConverter.ToMat convertToMat = new OpenCVFrameConverter.ToMat();
        final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(deviceType.value);
        if(this.executor.isPresent()){
            throw new IllegalStateException("The video retrieval thread has already been started.");
        }
        if(this.grabber.isPresent()){
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
            while(!Thread.interrupted()) {
                final Frame videoFrame;
                try {
                    videoFrame = grabber.grab();
                } catch (FrameGrabber.Exception e) {
                    throw new IllegalStateException("Failed to grab image", e);
                }
                frameOutputSocket.setValue(convertToMat.convert(videoFrame));
                long thisMoment = System.currentTimeMillis();
                frameRateOutputSocket.setValue(1000/(thisMoment - lastFrame));
                lastFrame = thisMoment;
            }
        });
        frameExecutor.setUncaughtExceptionHandler(
                (thread, exception) -> {
                    System.err.println("WebCam Frame Grabber Thread crashed with uncaught exception:");
                    exception.printStackTrace();
                }
        );
        frameExecutor.start();
        executor = Optional.of (frameExecutor);
    }

    /**
     * Stops the video feed from being
     * @throws TimeoutException If the thread running the WebCam fails to join this one after a timeout.
     */
    public void stopVideo() throws TimeoutException {
        if(executor.isPresent()){
            final Thread ex = executor.get();
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
                executor = Optional.empty();
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
            throw new IllegalStateException("Tried to stop a webcam that is already stopped.");
        }

    }

}
