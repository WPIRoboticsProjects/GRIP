package edu.wpi.grip.core.util;

import edu.wpi.grip.core.observables.Observable;

/**
 * An interface for pausable execution units.
 */
public interface Pausable {

  /**
   * Gets a property describing the current state of the pausable.
   */
  Observable<Boolean> pausedProperty();

  /**
   * Checks if execution is currently paused.
   */
  default boolean isPaused() {
    // poorly implemented subclasses may return null instead of true/false...
    return Boolean.TRUE.equals(pausedProperty().get());
  }

  /**
   * Pauses execution. NOP if already paused.
   */
  default void pause() {
    setPaused(true);
  }

  /**
   * Resumes execution. NOP if not paused.
   */
  default void resume() {
    setPaused(false);
  }

  /**
   * Sets whether this is paused or not.
   *
   * @param paused true if execution should be paused, false if execution should run
   */
  default void setPaused(boolean paused) {
    pausedProperty().set(paused);
  }

}
