package edu.wpi.grip.core.events;

import edu.wpi.grip.core.util.ExceptionWitness;

import java.util.Optional;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Indicates that an Exception has occurred. This class should not be constructed, instead use
 * {@link ExceptionWitness}
 */
public final class ExceptionEvent {
  private final Object origin;
  private final Optional<Exception> exception;
  private final String message;


  /**
   * @param origin    The object that triggered this exception.
   * @param exception The exception this is handling.
   * @param message   The message associated with this event. If {@code null} will use {@link
   *                  Exception#getMessage()}
   */
  public ExceptionEvent(Object origin, Exception exception, @Nullable String message) {
    this.exception = Optional.of(exception);
    this.origin = checkNotNull(origin, "The origin cannot be null");
    this.message = message == null ? exception.getMessage() : message;
  }

  /**
   * @param origin  The object that triggered this exception.
   * @param message The message associated with this event.
   */
  public ExceptionEvent(Object origin, String message) {
    this.origin = checkNotNull(origin, "The origin cannot be null");
    this.exception = Optional.empty();
    this.message = checkNotNull(message, "The message cannot be null");
  }

  /**
   * @param origin    The object that triggered this exception.
   * @param exception The exception this is handling. This will use {@link Exception#getMessage()}
   *                  for the message.
   */
  public ExceptionEvent(Object origin, Exception exception) {
    this(origin, exception, null);
  }


  public String getMessage() {
    return message;
  }

  public Object getOrigin() {
    return origin;
  }

  public Optional<Exception> getException() {
    return exception;
  }
}
