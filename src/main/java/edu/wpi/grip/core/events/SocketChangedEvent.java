package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Socket;

/**
 * An event that occurs when the value stored in a socket changes.  This can happen, for example, as the result of an
 * operation completing, or as a response to user input.
 */
public class SocketChangedEvent {
    final private Socket socket;

    /**
     * @param socket The socket that changed, with its new value.
     */
    public SocketChangedEvent(Socket socket) {
        this.socket = socket;
    }

    /**
     * @return The socket that changed, with its new value.
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
