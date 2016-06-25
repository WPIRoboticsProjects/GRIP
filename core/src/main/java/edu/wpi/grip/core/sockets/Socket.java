package edu.wpi.grip.core.sockets;


import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.Step;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A Socket is an abstract wrapper for a value that can be updated and passed around operations.
 * Sockets contain a set of hints about the data they contain, as well as an actual value. Sockets
 * that are given to operations are {@link InputSocket InputSockets}, and sockets that operations
 * store their results are {@link OutputSocket OutputSockets}.
 *
 * @param <T> The type of the value that this socket stores
 */
public interface Socket<T> {

  /**
   * @return A hint at what sort of data is in this socket.
   */
  SocketHint<T> getSocketHint();

  /**
   * Set the value of the socket using an {@link Optional}, and fire off a {@link
   * edu.wpi.grip.core.events.SocketChangedEvent}.
   *
   * @param optionalValue The optional value to assign this socket to.
   */
  void setValueOptional(Optional<? extends T> optionalValue);

  /**
   * Called when the value for the socket is reassigned. Can be used by an implementing class to
   * change behaviour when the value is changed.
   */
  default void onValueChanged() {
        /* no-op */
  }

  /**
   * @return The value currently stored in this socket.
   */
  Optional<T> getValue();

  /**
   * Set the value of the socket, and fire off a
   * {@link edu.wpi.grip.core.events.SocketChangedEvent}.
   *
   * @param value The value to store in this socket. Nullable.
   */
  default void setValue(@Nullable T value) {
    setValueOptional(Optional.ofNullable(getSocketHint().getType().cast(value)));
  }

  /**
   * If this socket is in a step return it.
   *
   * @return The step that this socket is part of
   * @see #getSource()
   */
  Optional<Step> getStep();

  /**
   * @param step The step that this socket is part of, if it's in a step.
   */
  void setStep(Optional<Step> step);

  /**
   * If this socket is in a source return it.
   *
   * @return The source that this socket is part of.
   * @see #getStep()
   */
  Optional<Source> getSource();

  /**
   * @param source The source that this socket is part of, if it's in a source.
   */
  void setSource(Optional<Source> source);

  /**
   * <code>INPUT</code> if this is the input to a step, <code>OUTPUT</code> if this is the output of
   * a step or source.
   *
   * @return The direction of the socket.
   */
  Direction getDirection();

  /**
   * @return The set of connections that have this socket as an input or output.
   */
  Set<Connection> getConnections();

  /**
   * Adds a connection to the socket.
   *
   * @param connection The connection to add to this socket.
   */
  void addConnection(Connection connection);

  /**
   * Removes a connnection from this socket.
   *
   * @param connection The connection to remove from this socket.
   */
  void removeConnection(Connection connection);

  enum Direction {
    INPUT, OUTPUT
  }
}
