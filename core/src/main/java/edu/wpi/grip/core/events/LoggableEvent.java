package edu.wpi.grip.core.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for loggable events.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggableEvent {

  /**
   * Possible logging levels. These have a 1-to-1 mapping with the default
   * {@link java.util.logging.Level logging levels}.
   */
  enum Level {
    FINEST(java.util.logging.Level.FINEST),
    FINER(java.util.logging.Level.FINER),
    FINE(java.util.logging.Level.FINE),
    CONFIG(java.util.logging.Level.CONFIG),
    INFO(java.util.logging.Level.INFO),
    WARNING(java.util.logging.Level.WARNING),
    SEVERE(java.util.logging.Level.SEVERE);

    private final java.util.logging.Level logLevel;

    Level(java.util.logging.Level logLevel) {
      this.logLevel = logLevel;
    }

    public java.util.logging.Level getLogLevel() {
      return logLevel;
    }
  }

  /**
   * The logging level to use. Defaults to {@link Level#INFO}.
   */
  Level level() default Level.INFO;

}
