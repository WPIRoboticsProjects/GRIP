package edu.wpi.grip.core.sockets;


import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.core.events.SocketConnectedChangedEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A concrete implementation of {@link Socket}
 * @param <T> The type of the value that this socket stores
 */
public class SocketImpl<T> implements Socket<T> {
    private final EventBus eventBus;
    private Optional<Step> step = Optional.empty();
    private Optional<Source> source = Optional.empty();
    private final Direction direction;
    private final Set<Connection> connections = new HashSet<>();
    private final SocketHint<T> socketHint;
    private Optional<? extends T> value = Optional.empty();


    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint {@link #getSocketHint}
     * @param direction  The direction that this socket represents
     */
    SocketImpl(EventBus eventBus, SocketHint<T> socketHint, Direction direction) {
        this.eventBus = checkNotNull(eventBus, "EventBus can not be null");
        this.socketHint = checkNotNull(socketHint, "Socket Hint can not be null");
        this.direction = checkNotNull(direction, "Direction can not be null");
    }

    @Override
    public SocketHint<T> getSocketHint() {
        return socketHint;
    }

    @Override
    public synchronized void setValueOptional(Optional<? extends T> optionalValue) {
        checkNotNull(optionalValue, "The optional value can not be null");
        if (optionalValue.isPresent()) {
            getSocketHint().getType().cast(optionalValue.get());
        }
        this.value = optionalValue;
        onValueChanged();
        eventBus.post(new SocketChangedEvent(this));
    }

    @Override
    public Optional<T> getValue() {
        if (!this.value.isPresent()) {
            this.value = socketHint.createInitialValue();
        }
        return (Optional<T>) this.value;
    }

    @Override
    public void setStep(Optional<Step> step) {
        step.ifPresent(s -> checkState(!this.source.isPresent(), "Socket cannot be both in a step and a source"));
        this.step = step;
    }

    @Override
    public void setSource(Optional<Source> source) {
        source.ifPresent(s -> checkState(!this.step.isPresent(), "Socket cannot be both in a step and a source"));
        this.source = source;
    }

    @Override
    public Optional<Step> getStep() {
        return step;
    }

    @Override
    public Optional<Source> getSource() {
        return source;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public Set<Connection> getConnections() {
        return ImmutableSet.copyOf(this.connections);
    }

    @Override
    public void addConnection(Connection connection) {
        checkNotNull(connection, "Can not remove null connection");
        this.connections.add(connection);

        if (this.connections.size() == 1) {
            this.eventBus.post(new SocketConnectedChangedEvent(this));
        }
    }

    @Override
    public void removeConnection(Connection connection) {
        checkNotNull(connection, "Can not remove null connection");
        this.connections.remove(connection);

        if (this.connections.isEmpty()) {
            this.eventBus.post(new SocketConnectedChangedEvent(this));
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
