package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Connection;

/**
 * An event that occurs when a connection is removed from the pipeline.  This is triggered by the user deleting a
 * connection with the GUI.
 */
public class ConnectionRemovedEvent {
    private Connection connection;

    /**
     * @param connection The connection being deleted
     */
    public ConnectionRemovedEvent(Connection connection) {
        this.connection = connection;
    }

    /**
     * @return The connection being deleted.
     */
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("connection", connection)
                .toString();
    }
}
