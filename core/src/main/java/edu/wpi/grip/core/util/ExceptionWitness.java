package edu.wpi.grip.core.util;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.events.ExceptionClearedEvent;
import edu.wpi.grip.core.events.ExceptionEvent;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Witnesses and reports exception. <b>This class is not suitable to handle {@link Error Errors}.</b><br />
 * {@link #flagException} should be used to flag the witness that an error has has occurred.
 * This will post an {@link ExceptionEvent} to the {@link EventBus}.
 * <br />
 * <br />
 * Example Usage:
 * <blockquote>
 * <pre>
 * {@code
 * while(true){
 *     try {
 *          // Do something that may throw an exception
 *          witness.clearException();
 *     } catch (Exception e) {
 *        witness.flagException(e, "There was a problem in this while loop");
 *     }
 * }
 * }
 * </pre>
 * <blockquote/>
 */
public class ExceptionWitness {
    private final EventBus eventBus;
    private final Object origin;
    private final AtomicBoolean isExceptionState = new AtomicBoolean(false);

    public interface Factory {
        ExceptionWitness create(Object origin);
    }

    @Inject
    ExceptionWitness(final EventBus eventBus, @Assisted final Object origin) {
        this.eventBus = eventBus;
        this.origin = origin;
    }

    /**
     * Indicates to the witness that an exception has occurred. This will also post an {@link ExceptionEvent} to the {@link EventBus}
     *
     * @param exception The exception that this is reporting.
     *                  If the Exception is an InterruptedException then this will not post an exception, instead,
     *                  it will set the threads interrupted state and return.
     * @param message   Any additional details that should be associated with this message.
     */
    public final void flagException(final Exception exception, @Nullable final String message) {
        if (exception instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            return;
        }
        isExceptionState.set(true);
        this.eventBus.post(new ExceptionEvent(origin, exception, message));
    }

    /**
     * @see #flagException(Exception, String)
     */
    public final void flagException(Exception exception) {
        flagException(exception, null);
    }

    /**
     * Allows a warning to be flagged without an exception. This should never be done when there is an exception
     * involved.
     *
     * @param warningMessage The message to flag.
     */
    public final void flagWarning(final String warningMessage) {
        isExceptionState.set(true);
        this.eventBus.post(new ExceptionEvent(origin, warningMessage));
    }

    /**
     * Indicate that there isn't currently an exception.
     * <p>
     * Clears the exception state and posts an {@link ExceptionClearedEvent}.
     * This method can be called every time that there isn't an exception as an {@link ExceptionClearedEvent} will
     * only be posted when there was previously an exception flagged.
     */
    public final void clearException() {
        // Only post an ExceptionClearedEvent if there was an exception before
        if (isExceptionState.compareAndSet(true, false)) {
            this.eventBus.post(new ExceptionClearedEvent(origin));
        }
    }

    /**
     * @return true if an exception has been flagged and not cleared
     */
    public final boolean isException() {
        return isExceptionState.get();
    }
}
