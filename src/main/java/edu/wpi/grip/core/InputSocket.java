package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;

/**
 * Represents the input into an {@link Operation}.
 * @param <T> The type of the value that this socket stores
 */
public class InputSocket<T> extends Socket<T> {

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint See {@link #getSocketHint()}
     * @param value      See {@link #getValue()}
     */
    public InputSocket(EventBus eventBus, SocketHint socketHint, T value) {
        super(eventBus, socketHint, value, Direction.INPUT);
    }

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint {@link #getSocketHint}
     */
    public InputSocket(EventBus eventBus, SocketHint<T> socketHint) {
        super(eventBus, socketHint, Direction.INPUT);
    }

}
