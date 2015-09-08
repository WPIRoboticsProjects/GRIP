package edu.wpi.grip.core;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.events.SocketChangedEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A socket is a wrapper for a value that can be updated and passed around operations.  Sockets contain a set of hints
 * about the data they contain, as well as an actual value.
 * <p/>
 * Sockets that are given to operations are referred to as "input sockets", and sockets that operations store their
 * results in are referred to as "output sockets".
 */
public class Socket<T> {
    private EventBus eventBus;
    private Step step;
    private SocketHint<T> socketHint;
    private Optional<T> value;

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
        }
    }

    /**
     * @return The value currently stored in this socket. If the value in this socket is not yet set it will
     * set the default value to the value and return it. If unset and no default it will return null.
     */
    public T getValue() {
        if (value.isPresent()) {
            return this.value.get();
        }
        T defaultValue = socketHint.getDefaultValue();
        if(defaultValue == null){
            return null;
        } else {
            this.setValue(defaultValue);
            return this.value.get();
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("socketHint", getSocketHint())
                .add("value", getValue())
                .toString();
    }
}
