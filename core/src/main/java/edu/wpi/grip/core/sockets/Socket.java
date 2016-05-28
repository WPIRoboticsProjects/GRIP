package edu.wpi.grip.core.sockets;


import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.Step;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

/**
 * A Socket is an abstract wrapper for a value that can be updated and passed around operations.  Sockets contain a set of hints
 * about the data they contain, as well as an actual value.
 * <p>
 * Sockets that are given to operations are {@link InputSocket InputSockets}, and sockets that operations store their
 * results are {@link OutputSocket OutputSockets}.
 *
 * @param <T> The type of the value that this socket stores
 */
public interface Socket<T> {
    enum Direction {INPUT, OUTPUT}

    /**
     * @return A hint at what sort of data is in this socket.
     */
    SocketHint<T> getSocketHint();

    /**
     * Set the value of the socket using an {@link Optional}, and fire off a {@link edu.wpi.grip.core.events.SocketChangedEvent}.
     *
     * @param optionalValue The optional value to assign this socket to.
     */
    void setValueOptional(Optional<? extends T> optionalValue);


    /**
     * Called when the value for the socket is reassigned.
     * Can be used by an implementing class to change behaviour when the value is changed.
     */
    default void onValueChanged() {
        /* no-op */
    }

    /**
     * Set the value of the socket, and fire off a {@link edu.wpi.grip.core.events.SocketChangedEvent}.
     *
     * @param value The value to store in this socket. Nullable.
     */
    default void setValue(@Nullable T value) {
        setValueOptional(Optional.ofNullable(getSocketHint().getType().cast(value)));
    }

    /**
     * @return The value currently stored in this socket.
     */
    Optional<T> getValue();

    /**
     * @param step The step that this socket is part of, if it's in a step.
     */
    void setStep(Optional<Step> step);

    /**
     * @return The step that this socket is part of
     * @see #getSource()
     */
    Optional<Step> getStep();

    /**
     * @param source The source that this socket is part of, if it's in a source.
     */
    void setSource(Optional<Source> source);

    /**
     * @return The source that this socket is part of
     * @see #getStep()
     */
    Optional<Source> getSource();

    /**
     * @return <code>INPUT</code> if this is the input to a step, <code>OUTPUT</code> if this is the output of a step
     * or source.
     */
    Direction getDirection();

    /**
     * @return The set of connections that have this socket as an input or output
     */
    Set<Connection> getConnections();

    /**
     * @param connection The connection to add to this socket.
     */
    void addConnection(Connection connection);

    /**
     * @param connection The connection to remove from this socket.
     */
    void removeConnection(Connection connection);
}
