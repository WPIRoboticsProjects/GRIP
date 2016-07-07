package edu.wpi.grip.core.events;

import edu.wpi.grip.core.Step;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event fired when a step has finished running.
 */
public class StepFinishedEvent {

  private final Step step;

  public StepFinishedEvent(Step step) {
    this.step = checkNotNull(step, "step");
  }

  public boolean isRegarding(Step step) {
    return this.step.equals(step);
  }

}
