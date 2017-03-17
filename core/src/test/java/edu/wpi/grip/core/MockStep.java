package edu.wpi.grip.core;

import edu.wpi.grip.core.metrics.MockTimer;
import edu.wpi.grip.core.util.MockExceptionWitness;

import com.google.common.eventbus.EventBus;

import java.util.Collections;

public class MockStep extends Step {

  public MockStep() {
    super(null,
        MockOperation.DESCRIPTION,
        Collections.emptyList(),
        Collections.emptyList(),
        origin -> null,
        source -> null);
  }

  public static Step createMockStepWithOperation() {
    final EventBus eventBus = new EventBus();
    return new Step.Factory(origin -> new MockExceptionWitness(eventBus, origin),
        source -> new MockTimer(eventBus, source))
        .create(new OperationMetaData(MockOperation.DESCRIPTION, MockOperation::new));
  }
}
