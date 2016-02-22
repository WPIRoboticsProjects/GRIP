package edu.wpi.grip.core.operations.network;


import java.util.Set;

/**
 * A network manager that handles all of the API overhead for dealing with
 * a specific network protocol.
 */
public interface Manager {
    /**
     *
     * @param keys The possible set of keys that this network publisher can publish.
     * @return A network publisher for the specific protocol.
     */
    <P> NetworkKeyValuePublisher<P> createPublisher(Class<P> publishType, Set<String> keys);
}
