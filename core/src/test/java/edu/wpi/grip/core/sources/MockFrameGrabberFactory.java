package edu.wpi.grip.core.sources;

import org.bytedeco.javacv.FrameGrabber;

import java.net.MalformedURLException;

/**
 * Frame Grabber Factory that mocks out the frame grabber that it returns
 */
public class MockFrameGrabberFactory implements CameraSource.FrameGrabberFactory {

    @Override
    public FrameGrabber create(int deviceNumber) {
        return new SimpleMockFrameGrabber();
    }

    @Override
    public FrameGrabber create(String addressProperty) throws MalformedURLException {
        return new SimpleMockFrameGrabber();
    }
}
