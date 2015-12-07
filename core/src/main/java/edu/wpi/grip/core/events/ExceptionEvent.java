package edu.wpi.grip.core.events;

import edu.wpi.grip.core.util.ExceptionWitness;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Indicates that an Exception has occurred.
 * This class should not be constructed, instead use {@link ExceptionWitness}
 */
public final class ExceptionEvent {
    private final Object origin;
    private final Optional<Exception> exception;
    private final String message;


    /**
     * @param origin    The object that triggered this exception.
     * @param exception The exception this is handling.
     * @param message   The message associated with this event.
     *                  If <tt>null</tt> will use {@link Exception#getMessage()}
     */
    public ExceptionEvent(Object origin, Exception exception, String message) {
        this.exception = Optional.of(exception);
        this.origin = checkNotNull(origin, "The origin can not be null");
        this.message = message != null ? message : exception.getMessage();
    }

    /**
     * @param origin  The object that triggered this exception.
     * @param message The message associated with this event.
     */
    public ExceptionEvent(Object origin, String message) {
        this.origin = checkNotNull(origin, "The origin can not be null");
        this.exception = Optional.empty();
        this.message = message;
    }

    /**
     * @param origin    The object that triggered this exception.
     * @param exception The exception this is handling.
     *                  This will use {@link Exception#getMessage()} for the message.
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
