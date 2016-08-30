package edu.wpi.grip.core;

import edu.wpi.grip.core.metrics.MockTimer;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/*
 * Do not extend this class. The object is registered in the constructor
 */
public final class ManualPipelineRunner extends PipelineRunner {

  @Inject
  public ManualPipelineRunner(EventBus eventBus, Pipeline pipeline) {
    super(eventBus, () -> pipeline, MockTimer.simpleFactory(eventBus));
    // This is fine because it is in a test
    eventBus.register(this);
  }

  @Override
  public PipelineRunner startAsync() {
    // NOPE
    return this;
  }

  @SuppressWarnings("PMD.UselessOverridingMethod")
  public void runPipeline() {
    super.runPipeline();
  }

}
