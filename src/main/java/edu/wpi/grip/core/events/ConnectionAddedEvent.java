package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Connection;

/**
 * An event that occurs when a new connection is added to the pipeline.  This is triggered by the user adding a
 * connection with the GUI.
 */
public class ConnectionAddedEvent implements AddedEvent {
    private final Connection connection;

    /**
     * @param connection The connection being added
     */
    public ConnectionAddedEvent(Connection connection) {
        this.connection = connection;
    }

    /**
     * @return The connection being added.
     */
    public Connection getConnection() {
        return this.connection;
    }

    public ConnectionRemovedEvent undoEvent() {
        return new ConnectionRemovedEvent(this.connection);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("connection", connection)
                .toString();
    }

    @Override
    public ConnectionRemovedEvent createUndoEvent() {
        return new ConnectionRemovedEvent(connection);
    }
}
