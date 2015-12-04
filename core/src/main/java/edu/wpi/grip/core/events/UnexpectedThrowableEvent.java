package edu.wpi.grip.core.events;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event should be thrown when Unexpected Throwable ends up in an {@link Thread#uncaughtExceptionHandler}.
 */
public final class UnexpectedThrowableEvent {
    private final Throwable throwable;
    private final boolean fatal;
    private final String message;

    /**
     * @param throwable The throwable that was caught.
     * @param message   Any additional information that should be displayed to the user. Nullable.
     * @param fatal     True if this cause the application to quit forcibly.
     *                  If the throwable is an {@link Error} than this is automatically true. Defaults to false.
     */
    public UnexpectedThrowableEvent(Throwable throwable, String message, boolean fatal) {
        this.throwable = checkNotNull(throwable, "Throwable can not be null");
        this.message = checkNotNull(message, "Message can not be null");
        this.fatal = (throwable instanceof Error) || fatal;
    }

    public UnexpectedThrowableEvent(Throwable throwable, String message) {
        this(throwable, message, false);
    }


    public Throwable getThrowable() {
        return throwable;
    }

    public String getMessage(){
        return message;
    }

    /**
     * @return True if this should cause the program to shutdown after it is handled
     */
    public boolean isFatal() {
        return fatal;
    }
}
