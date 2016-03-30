package edu.wpi.grip.core.sockets;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.Operation;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents the input into an {@link Operation}.
 *
 * @param <T> The type of the value that this socket stores
 */
@XStreamAlias(value = "grip:Input")
public class InputSocket<T> extends Socket<T> {
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint {@link #getSocketHint}
     */
    public InputSocket(EventBus eventBus, SocketHint<T> socketHint) {
        super(eventBus, socketHint, Direction.INPUT);
    }

    @Override
    protected void onValueChanged() {
        dirty.set(true);
    }

    /**
     * Checks if the socket has been dirtied and rests it to false.
     *
     * @return True if the socket has been dirtied
     */
    public boolean dirtied() {
        return dirty.compareAndSet(true, false);
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
