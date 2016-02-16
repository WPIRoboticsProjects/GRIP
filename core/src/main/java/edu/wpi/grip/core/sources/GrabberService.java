package edu.wpi.grip.core.sources;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.math.IntMath;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A service that manages the lifecycle of a {@link org.bytedeco.javacv.FrameGrabber}.
 */
public class GrabberService extends AbstractExecutionThreadService {
    private final String name;
    private final Supplier<FrameGrabber> frameGrabberSupplier;
    private final CameraSourceUpdater updater;
    private final Runnable exceptionClearedCallback;
    // Do not set this in the constructor.
    private FrameGrabber frameGrabber;

    /**
     * Keep a reference to the thread around so that it can be interrupted when stop is called.
     */
    private Optional<Thread> serviceThread = Optional.empty();

    GrabberService(String name, Supplier<FrameGrabber> frameGrabberSupplier, CameraSourceUpdater updater, Runnable exceptionClearedCallback) {
        super();
        this.name = checkNotNull(name, "Name cannot be null");
        this.frameGrabberSupplier = checkNotNull(frameGrabberSupplier, "Factory cannot be null");
        this.updater = checkNotNull(updater, "Updater cannot be null");
        this.exceptionClearedCallback = checkNotNull(exceptionClearedCallback, "Runnable cannot be null");
    }

    @Override
    protected void startUp() throws GrabberServiceException {
        serviceThread = Optional.of(Thread.currentThread());
        try {
            frameGrabber = frameGrabberSupplier.get();
            frameGrabber.start();
        } catch (FrameGrabber.Exception e) {
            throw new GrabberServiceException("Failed to start", e);
        }
    }

    @Override
    protected void run() throws GrabberServiceException {
        final OpenCVFrameConverter.ToMat convertToMat = new OpenCVFrameConverter.ToMat();
        final Stopwatch stopwatch = Stopwatch.createStarted();

        while (super.isRunning()) {
            runOneGrab(convertToMat, stopwatch);
        }
    }

    @VisibleForTesting
    final void runOneGrab(final OpenCVFrameConverter.ToMat convertToMat, final Stopwatch stopwatch) throws GrabberServiceException {
        final Frame videoFrame;
        try {
            videoFrame = frameGrabber.grab();
        } catch (FrameGrabber.Exception e) {
            throw new GrabberServiceException("Failed to grab image", e);
        }

        final opencv_core.Mat frameMat = convertToMat.convert(videoFrame);

        if (frameMat == null || frameMat.isNull()) {
            throw new GrabberServiceException("Returned a null frame Mat");
        }

        if (frameMat.empty()) {
            throw new GrabberServiceException("Returned an empty Mat");
        }

        updater.copyNewMat(frameMat);

        stopwatch.stop();
        final long elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        stopwatch.reset();
        stopwatch.start();
        if (elapsedTime != 0)
            updater.setFrameRate(IntMath.divide(1000, Math.toIntExact(elapsedTime), RoundingMode.DOWN));

        updater.updatesComplete();
        exceptionClearedCallback.run();
    }

    @Override
    protected void shutDown() throws GrabberServiceException {
        updater.setFrameRate(0);
        updater.updatesComplete();
        try {
            frameGrabber.stop();
        } catch (FrameGrabber.Exception e) {
            throw new GrabberServiceException("Failed to stop", e);
        }
    }

    @Override
    protected void triggerShutdown() {
        serviceThread.ifPresent(Thread::interrupt);
    }

    /*
     * Allows us to set our own service name
     */
    @Override
    protected String serviceName() {
        return name + " Service";
    }

    public final class GrabberServiceException extends IOException {

        GrabberServiceException(String message, Exception cause) {
            super("[" + name + "] " + message, cause);
        }

        GrabberServiceException(String message) {
            super("[" + name + "] " + message);
        }
    }
}
