package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import org.bytedeco.javacv.FrameGrabber;

import java.io.IOException;
import java.net.MalformedURLException;

public class MockCameraSource extends CameraSource {

    private boolean started = false;

    static class FrameGrabberFactory implements CameraSource.FrameGrabberFactory {

        @Override
        public FrameGrabber create(int deviceNumber) {
            return null;
        }

        @Override
        public FrameGrabber create(String addressProperty) throws MalformedURLException {
            return null;
        }
    }

    public MockCameraSource(EventBus eventBus, String address) throws IOException {
        super(eventBus, new FrameGrabberFactory(), origin -> null, address);
    }

    public MockCameraSource(EventBus eventBus, int deviceNumber) throws IOException {
        super(eventBus, new FrameGrabberFactory(), origin -> null, deviceNumber);
    }

    @Override
    public void start() throws IOException {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    public boolean isStarted() {
        return started;
    }
}
