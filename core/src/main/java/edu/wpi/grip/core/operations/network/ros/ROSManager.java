package edu.wpi.grip.core.operations.network.ros;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import edu.wpi.grip.core.operations.network.Manager;
import edu.wpi.grip.core.operations.network.NetworkKeyValuePublisher;
import org.ros.concurrent.CancellableLoop;
import org.ros.internal.message.Message;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Publisher;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
@Singleton
public class ROSManager implements Manager {
    /*
     * Be careful when declaring strings there are two types of the "String" class in this object.
     * Better to be verbose instead of assuming the compiler will figure out what type you are referring to.
     */

    ROSManager() {
        // no-op
    }

    private static class GRIPPublisherNode<P> extends AbstractNodeMain {
        private static final GraphName GRIP_ROOT = GraphName.of("GRIP/publisher");
        private final ConcurrentMap<java.lang.String, Object> keyValuePublishMap = Maps.newConcurrentMap();
        private final Class<P> publishType;

        private final GraphName nodeName;

        public GRIPPublisherNode(Class<P> publishType) {
            super();
            this.publishType = publishType;
            this.nodeName = GRIP_ROOT;
        }

        public GRIPPublisherNode(Class<P> publishType, GraphName nodeName) {
            super();
            this.publishType = publishType;
            this.nodeName = GRIP_ROOT.join(nodeName);
        }

        @Override
        public GraphName getDefaultNodeName() {
            return nodeName;
        }

        @Override
        public void onStart(final ConnectedNode connectedNode) {
            final Map<java.lang.String, Publisher<Message>> activePublishers = Maps.newHashMap();
            // This CancellableLoop will be canceled automatically when the node shuts
            // down.
            connectedNode.executeCancellableLoop(new CancellableLoop() {

                @Override
                protected void loop() throws InterruptedException {
                    final Map<java.lang.String, Publisher<Message>> usedKeyPublishers
                            = new HashMap<>(activePublishers.size());
                    keyValuePublishMap.forEach((key, value) -> {
                        // Get the ros type that maps to the specified value
                        final ROSType type = ROSType.resolveType(value);
                        // This can return null
                        Publisher<Message> publisher = activePublishers.get(key);
                        if (publisher == null) {
                            publisher = connectedNode.newPublisher(nodeName.join(GraphName.of(key)), type.getType());
                            publisher.setLatchMode(true);
                            activePublishers.put(key, publisher);
                        }
                        // Add this key and publisher to the set of ones used so it doesn't get removed
                        usedKeyPublishers.put(key, publisher);
                        final Message message = publisher.newMessage();
                        type.assignData(message, value);
                        publisher.publish(message);
                    });

                    // Collect the unused publishers
                    Sets.difference(activePublishers.keySet(), usedKeyPublishers.keySet())
                            // Copy into a new hash set so there isn't a concurrent modification exception
                            .copyInto(new HashSet<>())
                            // Remove the key so we only shut down the publisher once
                            // Shut them down because clearly we've stopped using them
                            .forEach(key -> activePublishers.remove(key).shutdown());
                }
            });
        }

        public void publish(Map<String, P> publishMap) {
            // This just ensures that the type is supported
            ROSType.resolveType(publishType);

        }

        public void publish(java.lang.String key, P value) {

        }

        public void stopPublish(java.lang.String key) {
            keyValuePublishMap.remove(key);
        }
    }

    private static final class ROSNetworkPublisher<P> extends NetworkKeyValuePublisher<P> {
        private final ImmutableSet<java.lang.String> keys;
        private Optional<java.lang.String> name = Optional.empty();

        private Optional<GRIPPublisherExecutorPair> publisherExecutor = Optional.empty();

        /**
         * Defines a mapping between the publisher and the executor that is running the publisher.
         */
        private static class GRIPPublisherExecutorPair<P> {
            private final GRIPPublisherNode<P> publisher;
            private final NodeMainExecutor nodeMainExecutor;

            private GRIPPublisherExecutorPair(GRIPPublisherNode<P> publisher, NodeMainExecutor nodeMainExecutor) {
                this.publisher = checkNotNull(publisher, "publisher cannot be null");
                this.nodeMainExecutor = checkNotNull(nodeMainExecutor, "nodeMainExecutor, cannot be null");
            }
        }

        protected ROSNetworkPublisher(Class<P> publishType, Set<java.lang.String> keys) {
            super(publishType, keys);
            this.keys = ImmutableSet.copyOf(keys);
        }

        private GRIPPublisherNode<P> createNewPublisher() {
            final GRIPPublisherNode<P> newPublisher;
            if (keys.isEmpty()) {
                newPublisher = new GRIPPublisherNode<>(getPublishType());
            } else {
                newPublisher = new GRIPPublisherNode<>(getPublishType(), GraphName.of(name.get()));
            }
            return newPublisher;
        }

        @Override
        protected final void publishNameChanged(Optional<java.lang.String> oldName, java.lang.String newName) {
            name = Optional.of(newName);
            // If there is already an executor, shut it down, we'll need to recreate it
            publisherExecutor.ifPresent(executorPair -> executorPair.nodeMainExecutor.shutdown());
            // Create the loader
            final ROSLoader loader = new ROSLoader();
            // Construct the node configurations
            final NodeConfiguration configuration = loader.build();
            // The executor will run the node
            final NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
            // The new node to be executed
            final GRIPPublisherNode<P> gripPublisherNode = createNewPublisher();
            // Start the node running
            nodeMainExecutor.execute(gripPublisherNode, configuration);
            // Save the new instance of the publisher
            publisherExecutor = Optional.of(new GRIPPublisherExecutorPair<>(gripPublisherNode, nodeMainExecutor));
        }

        @Override
        protected void doPublish(Map<java.lang.String, P> publishMap) {
            publisherExecutor.get().publisher.publish(getPublishType(), publishMap);
        }

        @Override
        protected void doPublish(P value) {
            publisherExecutor.get().publisher.publish(name.get(), value);
        }

        @Override
        public void doPublish() {
            publisherExecutor.get().publisher.stopPublish(name.get());
        }

        @Override
        public void close() {
            publisherExecutor.ifPresent(pe -> pe.nodeMainExecutor.shutdown());
            publisherExecutor = Optional.empty();
        }
    }

    @Override
    public <P> NetworkKeyValuePublisher<P> createPublisher(Class<P> publishType, Set<java.lang.String> keys) {
        return new ROSNetworkPublisher<>(publishType, keys);
    }

}
