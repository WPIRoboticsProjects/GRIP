package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.OperationMetaData;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event for when a new operation is added to the application.  This happens, for example, if a user loads a new Python
 * script, or at startup for the built-in operations.  This is NOT the event for adding a new step to the pipeline.
 */
public class OperationAddedEvent {
    private final OperationMetaData operation;

    /**
     * @param operation The operation being added
     */
    public OperationAddedEvent(OperationMetaData operation) {
        this.operation = checkNotNull(operation, "Operation cannot be null");
    }

    /**
     * @return The operation being added.
     */
    public OperationMetaData getOperation() {
        return this.operation;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("operation", operation)
                .toString();
    }
}
