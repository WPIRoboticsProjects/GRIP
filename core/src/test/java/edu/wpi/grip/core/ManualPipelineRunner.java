package edu.wpi.grip.core;

import edu.wpi.grip.core.metrics.MockTimer;

import com.google.common.eventbus.EventBus;

/*
 * Do not extend this class. The object is registered in the constructor
 */
public final class ManualPipelineRunner extends PipelineRunner {

  public ManualPipelineRunner(EventBus eventBus, Pipeline pipeline) {
    super(eventBus, () -> pipeline, MockTimer.simpleFactory(eventBus));
    // This is fine because it is in a test
    eventBus.register(this);
  }

  @Override
  public PipelineRunner startAsync() {
    throw new UnsupportedOperationException();
  }

  public void runPipeline() {
    super.runPipeline();
  }

}
