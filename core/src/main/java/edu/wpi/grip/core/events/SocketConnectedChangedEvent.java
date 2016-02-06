package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Socket;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is sent when the state of a socket (connected vs. disconnected) is changed.  Specifically, this will only
 * be called if the number of connections either goes to zero when it was non-zero, or when it was zero and one is
 * added.
 * <p>
 * The GUI listens to this event to determine how to render sockets and whether or not so show input controls for them.
 */
public class SocketConnectedChangedEvent {
    private final Socket socket;

    /**
     * @param socket The socket that was either connected or disconneted.
     */
    public SocketConnectedChangedEvent(Socket socket) {
        this.socket = checkNotNull(socket, "Socket cannot be null");
    }

    /**
     * @return The socket.
     */
    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("socket", socket)
                .toString();
    }
}
