package edu.wpi.grip.core.events;

import java.util.Optional;

/**
 * Should be posted when a non fatal user cause error occurs.
 * This is used to communicate to the main window that a
 * error should be displayed unobtrusively to the user.
 * The message that is displayed is the message in the Throwable.
 */
public class NotificationEvent {
    private final Throwable exception;
    private final Optional<String> detailedDescription;

    public NotificationEvent(Throwable exception){
        this(exception, null);
    }

    public NotificationEvent(Throwable exception, String detailedDescription) {
        this.exception = exception;
        this.detailedDescription = Optional.ofNullable(detailedDescription);
    }

    public Throwable getException() {
        return exception;
    }

    public Optional<String> getDetailedDescription(){
        return detailedDescription;
    }
}
