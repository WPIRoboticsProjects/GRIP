package edu.wpi.grip.core.events;

import edu.wpi.grip.core.Step;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event fired when a step has started to be run. This will be accompanied by a
 * {@link StepFinishedEvent} after the step has finished running.
 */
public class StepStartedEvent {

  private final Step step;

  public StepStartedEvent(Step step) {
    this.step = checkNotNull(step, "step");
  }

  public boolean isRegarding(Step step) {
    return this.step.equals(step);
  }

}
