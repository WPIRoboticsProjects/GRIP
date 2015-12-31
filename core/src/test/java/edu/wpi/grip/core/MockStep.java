package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;

import java.util.Optional;

public class MockStep extends Step {

    public MockStep() {
        super(null, new InputSocket[0], new OutputSocket[0], Optional.empty());
    }

    public static Step createMockStepWithOperation() {
        return new Step.Factory(new EventBus()).create(new MockOperation());
    }
}
