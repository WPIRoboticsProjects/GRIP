package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Socket;

/**
 * An event that occurs when a socket is set to be either previewed or not previewed.  The GUI listens for these events
 * so it knows which sockets to show previews for.
 */
public class SocketPreviewChangedEvent {
    private Socket socket;

    /**
     * @param socket The socket being previewed
     */
    public SocketPreviewChangedEvent(Socket socket) {
        this.socket = socket;
    }

    /**
     * @return The socket being previewed
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
