package edu.wpi.grip.core;

import edu.wpi.grip.core.util.MockExceptionWitness;

import com.fasterxml.uuid.Generators;
import com.google.common.eventbus.EventBus;

import java.util.Collections;
import java.util.UUID;

public class MockStep extends Step {

  public MockStep() {
    super(null, MockOperation.DESCRIPTION, Collections.emptyList(), Collections.emptyList(),
        origin -> null, UUID.randomUUID());
  }

  public static Step createMockStepWithOperation() {
    final EventBus eventBus = new EventBus();
    return createStepFactory(eventBus).create(
        new OperationMetaData(MockOperation.DESCRIPTION, MockOperation::new));
  }

  public static Step.Factory createStepFactory(EventBus eventBus) {
    return new Step.Factory(
        Generators.timeBasedGenerator(),
        origin -> new MockExceptionWitness(eventBus, origin));
  }

  public static Step.Factory createStepFactory() {
    return createStepFactory(new EventBus());
  }
}
