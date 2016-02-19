package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.util.MockExceptionWitness;
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
        super(eventBus, new FrameGrabberFactory(), MockExceptionWitness.MOCK_FACTORY, address);
    }

    public MockCameraSource(EventBus eventBus, int deviceNumber) throws IOException {
        super(eventBus, new FrameGrabberFactory(), MockExceptionWitness.MOCK_FACTORY, deviceNumber);
    }

    @Override
    public MockCameraSource startAsync() {
        started = true;
        return this;
    }

    @Override
    public MockCameraSource stopAsync() {
        started = false;
        return this;
    }

    public boolean isRunning() {
        return started;
    }
}
