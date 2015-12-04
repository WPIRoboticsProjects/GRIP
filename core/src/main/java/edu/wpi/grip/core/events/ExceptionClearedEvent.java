package edu.wpi.grip.core.events;

import edu.wpi.grip.core.util.ExceptionWitness;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Indicates that an flaggedException, originally indicated by a {@link ExceptionEvent}, has been resolved.
 * This class should not be constructed. Instead, use {@link ExceptionWitness}
 */
public class ExceptionClearedEvent {
    private final Object origin;

    public ExceptionClearedEvent(Object origin) {
        this.origin = checkNotNull(origin, "The origin can not be null");
    }

    public Object getOrigin() {
        return origin;
    }
}
