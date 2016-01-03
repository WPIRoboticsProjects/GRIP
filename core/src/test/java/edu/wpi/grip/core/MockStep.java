package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.util.MockExceptionWitness;

import java.util.Optional;

public class MockStep extends Step {

    public MockStep() {
        super(null, new InputSocket[0], new OutputSocket[0], Optional.empty(), origin -> null);
    }

    public static Step createMockStepWithOperation() {
        final EventBus eventBus = new EventBus();
        return new Step.Factory(eventBus, origin -> new MockExceptionWitness(eventBus, origin)).create(new MockOperation());
    }
}
