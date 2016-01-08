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
     * @param socketHint {@link #getSocketHint}
     */
    public InputSocket(EventBus eventBus, SocketHint<T> socketHint) {
        super(eventBus, socketHint, Direction.INPUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDisconnected() {
        super.onDisconnected();
        if (this.getConnections().isEmpty()) {
            this.setValue(this.getSocketHint().createInitialValue().orElse(null));
        }
    }
}
