package edu.wpi.grip.core;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.*;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A socket is a wrapper for a value that can be updated and passed around operations.  Sockets contain a set of hints
 * about the data they contain, as well as an actual value.
 * <p/>
 * Sockets that are given to operations are referred to as "input sockets", and sockets that operations store their
 * results in are referred to as "output sockets".
 */
public class Socket<T> {
    private final EventBus eventBus;
    private Step step;
    private final Set<Connection> connections = new HashSet<>();
    private final SocketHint<T> socketHint;
    private Optional<T> value;
    private boolean published = false;

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint See {@link #getSocketHint()}
     * @param value      See {@link #getValue()}
     */
    public Socket(EventBus eventBus, SocketHint<T> socketHint, T value) {
        this.eventBus = eventBus;
        this.socketHint = socketHint;
        this.value = Optional.of(value);

        checkNotNull(eventBus);
        checkNotNull(socketHint);
        checkNotNull(value);

        this.eventBus.register(this);
    }

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint {@link #getSocketHint}
     */
    public Socket(EventBus eventBus, SocketHint<T> socketHint) {
        this.eventBus = eventBus;
        this.socketHint = socketHint;
        this.value = Optional.absent();

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
     * Set the value of the socket, and fire off a {@link edu.wpi.grip.core.events.SocketChangedEvent} if the value is
     * different from the current one.
     *
     * @param value The value to store in this socket.
     */
    public void setValue(T value) {
        if (!this.value.isPresent() || !this.value.get().equals(value)) {
            this.value = Optional.of(this.getSocketHint().getType().cast(value));
            eventBus.post(new SocketChangedEvent(this));

            // If the socket's value is set to be published, also send a SocketPublishedEvent to notify any sinks that
            // it has changed.
            if (this.isPublished()) {
                eventBus.post(new SocketPublishedEvent(this));
            }
        }
    }

    /**
     * @return The value currently stored in this socket.
     */
    public T getValue() {
        if (value.isPresent()) {
            return value.get();
        } else {
            return socketHint.getDefaultValue();
        }
    }

    /**
     * @param step The step that this socket is part of.
     */
    public void setStep(Step step) {
        this.step = step;
    }

    /**
     * @return The step that this socket is part of, or <code>null</code> if it has not been set.
     */
    public Step getStep() {
        return step;
    }

    /**
     * @return The set of connections that have this socket as an input or output
     */
    public Set<Connection> getConnections() {
        return ImmutableSet.copyOf(this.connections);
    }

    /**
     * @param published If <code>true</code>, this socket will be published by any sink that is currently active.  For
     *                  example, it may be set as a NetworkTables value.
     */
    public void setPublished(boolean published) {
        // If the socket wasn't previously published and is now, send a SocketPublishedEvent to publish an initial
        // value.
        if (published && !isPublished()) {
            eventBus.post(new SocketPublishedEvent(this));
        }

        this.published = published;
    }

    /**
     * @return Weather or not this socket should be published.
     * @see #setPublished(boolean)
     */
    public boolean isPublished() {
        return this.published;
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
                .add("published", isPublished())
                .add("connections", getConnections())
                .toString();
    }
}
