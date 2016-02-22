package edu.wpi.grip.core.operations.network;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages the interface between the {@link KeyValuePublishOperation} and the actual network
 * protocol implemented by a specific {@link Manager}.
 * <p>
 * This class is designed to be in one of two states, either the set of keys
 * will be empty. In which case the name will be used as the publish key.
 * In the other case the keys list will be populated and each value will
 * need to be published with a specific key.
 */
public abstract class NetworkKeyValuePublisher<T> extends NetworkPublisher {
    private final ImmutableSet<String> keys;
    private final Class<T> publishType;

    protected NetworkKeyValuePublisher(Class<T> publishType, Set<String> keys) {
        checkArgument(!keys.contains(""), "Keys can not contain the empty string");
        this.keys = ImmutableSet.copyOf(keys);
        this.publishType = checkNotNull(publishType, "The publishType cannot be null");
    }

    protected final Class<T> getPublishType() {
        return publishType;
    }

    /**
     * Publishes the given key/value pair to the network publisher.
     * The key must be in the set of keys provided to the constructor.
     *
     * @param publishMap the keyValues to publish
     */
    final void publish(Map<String, T> publishMap) {
        final Map<String, T> publishMapCopy = ImmutableMap.copyOf(publishMap);
        if (!keys.isEmpty()) {
            publishMap.entrySet().forEach(key -> checkArgument(keys.contains(key), "Key must be in keys list: " + key));
        }
        checkNamePresent();
        if (!publishMapCopy.containsKey("")) {
            doPublish(publishMapCopy);
        } else if(!publishMapCopy.keySet().isEmpty()){
            doPublish(publishMapCopy.get(""));
        } else {
            doPublish();
        }
    }

    /**
     * Perform the publish with the key and value. The key is guaranteed to be in the
     * {@link #keys} set.
     *
     * @param publishMap the keyValues to publish
     */
    protected abstract void doPublish(Map<String, T> publishMap);

    /**
     * Publish a single value using the {@link #name} as the key.
     *
     * @param value The value to be published
     */
    protected abstract void doPublish(T value);

    /**
     * Publishes nothing.
     */
    protected abstract void doPublish();

    /**
     * Close the network publisher. This should not throw an exception.
     */
    public abstract void close();
}
