package edu.wpi.grip.core.events;

import java.util.logging.Level;

/**
 * Interface for loggable events.
 */
public interface LoggableEvent {

  /**
   * Gets log level of this event. Defaults to {@link Level#INFO}.
   */
  default Level logLevel() {
    return Level.INFO;
  }

  /**
   * Creates a string representation of this event to be logged. Defaults to {@code toString()}.
   */
  default String asLoggableString() {
    return toString();
  }

}
