package edu.wpi.grip.core;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Socket is an abstract wrapper for a value that can be updated and passed around operations.  Sockets contain a set of hints
 * about the data they contain, as well as an actual value.
 * <p>
 * Sockets that are given to operations are {@link InputSocket InputSockets}, and sockets that operations store their
 * results are {@link OutputSocket OutputSockets}.
 *
 * @param <T> The type of the value that this socket stores
 */
public abstract class Socket<T> {
    public enum Direction {INPUT, OUTPUT}
    public enum SocketStatus {VALID, POSSIBLE_INVALID, INVALID}

    protected final EventBus eventBus;
    private Optional<Step> step = Optional.empty();
    private final Direction direction;
    private final Set<Connection> connections = new HashSet<>();
    private final SocketHint<T> socketHint;
    private SocketStatus status = SocketStatus.VALID;
    private T value;

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint See {@link #getSocketHint()}
     * @param value      See {@link #getValue()}
     * @param direction  The direction that this socket represents
     */
    public Socket(EventBus eventBus, SocketHint<T> socketHint, T value, Direction direction) {
        this.eventBus = eventBus;
        this.socketHint = socketHint;
        this.value = value;
        this.direction = direction;

        checkNotNull(eventBus);
        checkNotNull(socketHint);
        checkNotNull(value);
        checkNotNull(direction);

        this.eventBus.register(this);
    }

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint {@link #getSocketHint}
     * @param direction  The direction that this socket represents
     */
    public Socket(EventBus eventBus, SocketHint<T> socketHint, Direction direction) {
        this.eventBus = eventBus;
        this.socketHint = socketHint;
        this.value = socketHint.createInitialValue();
        this.direction = direction;

        checkNotNull(eventBus);
        checkNotNull(socketHint);

        this.eventBus.register(this);
    }

    /**
     * @return A hint at what sort of data is in this socket.
     */
    public SocketHint<T> getSocketHint() {
        return socketHint;
    }

    /**
     * Set the value of the socket, and fire off a {@link edu.wpi.grip.core.events.SocketChangedEvent}.
     *
     * @param value The value to store in this socket.
     */
    public void setValue(T value) {
        this.value = this.getSocketHint().getType().cast(value);
        eventBus.post(new SocketChangedEvent(this));
    }

    /**
     * @return The value currently stored in this socket.
     */
    public T getValue() {
        return this.value;
    }

    /**
     * @param step The step that this socket is part of.
     */
    public void setStep(Optional<Step> step) {
        this.step = step;
    }

    /**
     * @return The step that this socket is part of, or <code>null</code> if it has not been set.
     */
    public Optional<Step> getStep() {
        return step;
    }


    /**
     * @return <code>INPUT</code> if this is the input to a step or sink, <code>OUTPUT</code> if this is the output of
     * a step or source
     */
    public Direction getDirection() {
        return this.direction;
    }

    /**
     * @return The set of connections that have this socket as an input or output
     */
    public Set<Connection> getConnections() {
        return ImmutableSet.copyOf(this.connections);
    }

    public void setStatus(SocketStatus status){
        if(!this.status.equals(status)){
            this.status = status;
            this.eventBus.post(new SocketStatusChangedEvent(this));
        }
    }

    public SocketStatus getStatus(){
        return this.status;
    }


    @Subscribe
    public void onConnectionAdded(ConnectionAddedEvent event) {
        if (event.getConnection().getInputSocket() == this || event.getConnection().getOutputSocket() == this) {
            this.connections.add(event.getConnection());

            if (this.connections.size() == 1) {
                this.eventBus.post(new SocketConnectedChangedEvent(this));
            }
        }
    }

    @Subscribe
    public void onConnectionRemoved(ConnectionRemovedEvent event) {
        if (event.getConnection().getInputSocket() == this || event.getConnection().getOutputSocket() == this) {
            this.connections.remove(event.getConnection());

            if (this.connections.isEmpty()) {
                this.eventBus.post(new SocketConnectedChangedEvent(this));
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("socketHint", getSocketHint())
                .add("value", getValue())
                .add("direction", getDirection())
                .toString();
    }
}
