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
    private final Exception exception;
    private final Optional<String> message;

    public ExceptionEvent(Object origin, Exception exception) {
        this(origin, exception, null);
    }

    public ExceptionEvent(Object origin, Exception exception, String message) {
        this.exception = checkNotNull(exception, "The exception can not be null");
        this.origin = checkNotNull(origin, "The origin can not be null");
        this.message = Optional.ofNullable(message);
    }

    public Optional<String> getMessage() {
        return message;
    }

    public Object getOrigin() {
        return origin;
    }

    public Exception getException() {
        return exception;
    }
}
