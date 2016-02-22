package edu.wpi.grip.core.operations.network;


public abstract class NetworkValuePublisher extends NetworkPublisher {

    /**
     * Publishes the given value to the newtwork publisher.
     * The key will be the {@link #name}.
     * The key set must be empty for this to be valid.
     *
     * @param value The value to publish.
     */
    final void publish(Object value) {
        checkNamePresent();
        doPublish(value);
    }

    /**
     * Publish a single value using the {@link #name} as the key.
     *
     * @param value The value to be published
     */
    protected abstract void doPublish(Object value);

    /**
     * Stops publishing the given value and does any cleanup necessary.
     */
    protected abstract void stopPublish();

    public abstract void close();
}
