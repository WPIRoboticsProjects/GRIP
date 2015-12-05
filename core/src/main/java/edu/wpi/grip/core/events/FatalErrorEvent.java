package edu.wpi.grip.core.events;

/**
 * Event should be thrown when a fatal event occurs.
 */
public class FatalErrorEvent {
    private Throwable throwable;

    /**
     * @param throwable The exception that was caught
     */
    public FatalErrorEvent(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
