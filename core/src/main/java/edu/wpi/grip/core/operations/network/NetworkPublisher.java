package edu.wpi.grip.core.operations.network;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Manages the interface between the {@link PublishAnnotatedOperation} and the actual network
 * protocol implemented by a specific {@link Manager}.
 * <p>
 * This class is designed to be in one of two states, either the set of keys
 * will be empty. In which case the name will be used as the publish key.
 * In the other case the keys list will be populated and each value will
 * need to be published with a specific key.
 *
 * @param <T> The type of value to be published
 */
public abstract class NetworkPublisher<T> implements AutoCloseable {
    private Optional<String> name = Optional.empty();

    protected NetworkPublisher() {
        /* empty */
    }

    /**
     * Sets the name for the publisher.
     * It is acceptable to set the name to the existing name without consequence.
     * If the name for the publisher changes {@link #publishNameChanged(Optional, String)}
     * will be called.
     *
     * @param name The new/existing name for the publisher.
     */
    public final void setName(String name) {
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
     * Should be called to pass a value to be published to the NetworkPublisher
     *
     * @param publish The value to publish
     */
    public abstract void publish(T publish);

    /**
     * Called when when the name assigned to the publisher changes.
     * This method should not be called directly, instead, call {@link #setName(String)}
     *
     * @param oldName the old name for the publisher. Will be empty if the name was never set before.
     * @param newName the new name for the publisher. Will not be an empty string.
     */
    protected abstract void publishNameChanged(Optional<String> oldName, String newName);

    /**
     * Close the network publisher. This should not throw an exception.
     */
    public abstract void close();
}
