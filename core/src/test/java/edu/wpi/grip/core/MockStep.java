package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;

public class MockStep extends Step {

    public MockStep() {
        super(null, null, null, null);
    }

    public static Step createMockStepWithOperation(){
        return new Step.Factory(new EventBus()).create(new MockOperation());
    }
}
