package edu.wpi.grip.core.operations.network;


import java.util.Set;

/**
 * A factory to create {@link MapNetworkPublisher MapNetworkPublishers}.
 */
@FunctionalInterface
public interface MapNetworkPublisherFactory {
  <T> MapNetworkPublisher<T> create(Set<String> keys);
}
