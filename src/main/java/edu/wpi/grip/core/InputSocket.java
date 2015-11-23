package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.events.SocketConnectedChangedEvent;

/**
 * Represents the input into an {@link Operation}.
 *
 * @param <T> The type of the value that this socket stores
 */
@XStreamAlias(value = "grip:Input")
public class InputSocket<T> extends Socket<T> {

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint See {@link #getSocketHint()}
     * @param value      See {@link #getValue()}
     */
    public InputSocket(EventBus eventBus, SocketHint<T> socketHint, T value) {
        super(eventBus, socketHint, value, Direction.INPUT);
    }

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint {@link #getSocketHint}
     */
    public InputSocket(EventBus eventBus, SocketHint<T> socketHint) {
        super(eventBus, socketHint, Direction.INPUT);
    }

    /**
     * Reset the socket to its default value when it's no longer connected to anything.  This prevents removed
     * connections from continuing to have an effect on steps because they still hold references to the values they
     * were connected to.
     */
    @Subscribe
    public void onDisconnected(SocketConnectedChangedEvent event) {
        if (event.getSocket() == this && this.getConnections().isEmpty()) {
            this.setValue(this.getSocketHint().createInitialValue());
        }
    }
}
