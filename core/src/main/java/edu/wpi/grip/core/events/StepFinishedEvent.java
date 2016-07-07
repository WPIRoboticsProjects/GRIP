package edu.wpi.grip.core.events;

import edu.wpi.grip.core.Step;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event fired when a step has finished running.
 */
public class StepFinishedEvent {

  private final Step step;

  /**
   * Creates a new {@code StepFinishedEvent}.
   *
   * @param step the step that was finished
   */
  public StepFinishedEvent(Step step) {
    this.step = checkNotNull(step, "step");
  }

  /**
   * Checks if the given step is the one that this event is for.
   *
   * @param step the step to check
   * @return true if this event is for the given event, false if it isn't
   */
  public boolean isRegarding(@Nullable Step step) {
    return this.step.equals(step);
  }

}
