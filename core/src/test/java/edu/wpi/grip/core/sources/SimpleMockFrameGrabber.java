package edu.wpi.grip.core.sources;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "PMD.SignatureDeclareThrowsException"})
class SimpleMockFrameGrabber extends FrameGrabber {

    @Override
    public void start() throws Exception {
        /* no-op */
    }

    @Override
    public void stop() throws Exception {
        /* no-op */
    }

    @Override
    public void trigger() throws Exception {
        /* no-op */
    }

    @Override
    public Frame grab() throws Exception {
        return null;
    }

    @Override
    public void release() throws Exception {
        /* no-op */
    }
}
