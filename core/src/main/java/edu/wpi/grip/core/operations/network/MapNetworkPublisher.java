package edu.wpi.grip.core.operations.network;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Publishes a map of keys to values.
 *
 * @param <T> The type of the 'value' in the string, value map that is being published
 */
public abstract class MapNetworkPublisher<T> extends NetworkPublisher<Map<String, T>> {
  private final ImmutableSet<String> keys;

  protected MapNetworkPublisher(Set<String> keys) {
    super();
    checkArgument(!keys.contains(""), "Keys can not contain the empty string");
    this.keys = ImmutableSet.copyOf(keys);
  }

  /**
   * Publishes the given key/value pair to the network publisher. The key must be in the set of keys
   * provided to the constructor.
   *
   * @param publishMap the keyValues to publish
   */
  @Override
  public final void publish(Map<String, T> publishMap) {
    final Map<String, T> publishMapCopy = ImmutableMap.copyOf(publishMap);
    if (!keys.isEmpty()) {
      publishMap.keySet().forEach(key -> checkArgument(keys.contains(key), "Key must be in keys "
          + "list: " + key));
    }
    checkNamePresent();
    if (!publishMapCopy.containsKey("") && !keys.isEmpty()) {
      doPublish(publishMapCopy);
    } else if (!publishMapCopy.keySet().isEmpty()) {
      doPublishSingle(publishMapCopy.get(""));
    } else {
      doPublish();
    }
  }

  /**
   * Publishes nothing.
   */
  protected abstract void doPublish();

  /**
   * Publishes a key value mapping.
   *
   * @param publishMap The mapping of keys to values to be published
   */
  protected abstract void doPublish(Map<String, T> publishMap);

  /**
   * Publish a single value using the {@link #name} as the key.
   *
   * @param value The value to be published
   */
  protected abstract void doPublishSingle(T value);
}
