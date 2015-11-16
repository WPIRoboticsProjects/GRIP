package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.OutputSocket;

/**
 * An event that occurs when a socket should be published to an external output "sink", such as NetworkTables.
 */
public class SocketPublishedEvent {
    private OutputSocket socket;

    /**
     * @param socket The socket that should be published
     */
    public SocketPublishedEvent(OutputSocket socket) {
        this.socket = socket;
    }

    /**
     * @return The socket that should be published
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
