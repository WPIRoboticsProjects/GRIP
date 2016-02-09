package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.OutputSocket;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event that occurs when a {@link OutputSocket} is set to be either previewed or not previewed.  The GUI listens for these events
 * so it knows which sockets to show previews for.
 */
public class SocketPreviewChangedEvent {
    private OutputSocket socket;

    /**
     * @param socket The socket being previewed
     */
    public SocketPreviewChangedEvent(OutputSocket socket) {
        this.socket = checkNotNull(socket, "Socket cannot be null");
    }

    /**
     * @return The socket being previewed
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
