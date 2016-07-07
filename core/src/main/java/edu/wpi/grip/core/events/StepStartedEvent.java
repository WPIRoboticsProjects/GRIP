package edu.wpi.grip.core.events;

import edu.wpi.grip.core.Step;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event fired when a step has started to be run. This will be accompanied by a
 * {@link StepFinishedEvent} after the step has finished running.
 */
public class StepStartedEvent {

  private final Step step;

  /**
   * Creates a new {@code StepStartedEvent}.
   *
   * @param step the step that was started
   */
  public StepStartedEvent(Step step) {
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
