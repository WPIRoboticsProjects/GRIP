package edu.wpi.grip.core.events;

import edu.wpi.grip.core.sockets.OutputSocket;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event that occurs when a {@link OutputSocket} is set to be either previewed or not previewed.
 * The GUI listens for these events so it knows which sockets to show previews for.
 */
public class SocketPreviewChangedEvent {
  private final OutputSocket socket;

  /**
   * @param socket The socket being previewed.
   */
  public SocketPreviewChangedEvent(OutputSocket socket) {
    this.socket = checkNotNull(socket, "Socket cannot be null");
  }

  /**
   * Queries the event to determine if this event is about this socket.
   *
   * @param socket The socket to check to see if it is related to.
   * @return True if this socket is with regards to this event.
   */
  public boolean isRegarding(OutputSocket socket) {
    // This is necessary as some of the sockets are just decorators for other sockets.
    return socket.equals(this.socket);
  }

  /**
   * @return The socket being previewed.
   */
  public OutputSocket getSocket() {
    return this.socket;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("socket", socket)
        .toString();
  }
}
