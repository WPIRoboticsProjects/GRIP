package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.util.MockExceptionWitness;

import java.io.IOException;

public class MockCameraSource extends CameraSource {

    private boolean started = false;

    public MockCameraSource(EventBus eventBus, String address) throws IOException {
        super(eventBus, new MockOutputSocketFactory(eventBus), new MockFrameGrabberFactory(), MockExceptionWitness.MOCK_FACTORY, address);
    }

    public MockCameraSource(EventBus eventBus, int deviceNumber) throws IOException {
        super(eventBus, new MockOutputSocketFactory(eventBus), new MockFrameGrabberFactory(), MockExceptionWitness.MOCK_FACTORY, deviceNumber);
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
