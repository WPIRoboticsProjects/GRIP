package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Socket;

/**
 * An event that occurs when a socket should be published to an external output "sink", such as NetworkTables.
 */
public class SocketPublishedEvent {
    private Socket socket;

    /**
     * @param socket The socket that should be published
     */
    public SocketPublishedEvent(Socket socket) {
        this.socket = socket;
    }

    /**
     * @return The socket that should be published
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
