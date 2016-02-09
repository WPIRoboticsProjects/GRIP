package edu.wpi.grip.core.events;

import edu.wpi.grip.core.util.SafeShutdown;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event should be thrown when Unexpected Throwable ends up in an {@link Thread#uncaughtExceptionHandler}.
 * This event may potentially only be handled once if the exception is deemed fatal.
 */
public final class UnexpectedThrowableEvent {
    private final Logger logger = Logger.getLogger(getClass().getName());
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


    /**
     * Handles the exception, this method can only be called once if the event is fatal.
     * If the event is fatal then this method will never return.
     *
     * @param handler Handles the exception but in the safest way possible.
     *                Will not be run if throwable is an interrupted exception.
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void handleSafely(UnexpectedThrowableEventHandler handler) {
        checkNotNull(handler, "Handler cannot be null");
        try {
            if (!(throwable instanceof InterruptedException)) {
                handler.handle(throwable, message, fatal);
            }
        } catch (Throwable throwable) {
            // The handler threw an exception, something is really wrong here.
            try {
                logger.log(Level.SEVERE, "Failed to handle safely", throwable);
            } finally {
                SafeShutdown.exit(1);
            }
        } finally {
            shutdownIfFatal();
        }
    }

    /**
     * This method will not return if the event is fatal.
     */
    public void shutdownIfFatal() {
        if (fatal) {
            try {
                // We try logging, it may not work.
                logger.log(Level.SEVERE, "Shutting down from error", throwable);
            } finally {
                // If all else fails then shutdown
                SafeShutdown.exit(1);
            }
        }
    }

    /**
     * @return True if this should cause the program to shutdown after it is handled
     */
    public boolean isFatal() {
        return fatal;
    }

    /**
     * A functional interface allowing for UnexpectedThrowableEvents to be handled safely.
     */
    @FunctionalInterface
    public interface UnexpectedThrowableEventHandler {
        void handle(Throwable throwable, String message, boolean isFatal);
    }
}
