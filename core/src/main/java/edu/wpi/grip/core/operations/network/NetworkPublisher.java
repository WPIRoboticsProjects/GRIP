package edu.wpi.grip.core.operations.network;


import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class NetworkPublisher implements AutoCloseable {
    private Optional<String> name = Optional.empty();

    /**
     * Sets the name for the publisher.
     * It is acceptable to set the name to the existing name without consequence.
     * If the name for the publisher changes {@link #publishNameChanged(Optional, String)}
     * will be called.
     *
     * @param name The new/existing name for the publisher.
     */
    final void setName(String name) {
        checkArgument(!name.isEmpty(), "Name cannot be an empty string");
        if (this.name.equals(Optional.of(name))) {
            // The old name and the new name are the same
            return;
        }
        publishNameChanged(this.name, name);
        this.name = Optional.of(name);
    }

    /**
     * Checks that the name is present before performing any operation.
     */
    protected final void checkNamePresent() {
        if (!name.isPresent()) {
            throw new IllegalStateException("Name must be set");
        }
    }

    /**
     * Called when when the name assigned to the publisher changes.
     * This method should not be called directly, instead, call {@link #setName(String)}
     *
     * @param oldName the old name for the publisher. Will be empty if the name was never set before.
     * @param newName the new name for the publisher. Will not be an empty string.
     */
    protected abstract void publishNameChanged(Optional<String> oldName, String newName);

}
