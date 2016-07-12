package edu.wpi.grip.core.operations.network.ros;


import edu.wpi.grip.core.operations.network.Manager;
import edu.wpi.grip.core.util.SinglePermitSemaphore;

import com.google.inject.Singleton;

import org.ros.concurrent.CancellableLoop;
import org.ros.internal.message.Message;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages the lifecycle of ROS Nodes.
 */
@Singleton
public class ROSManager implements Manager, ROSNetworkPublisherFactory {
  /*
   * Be careful when declaring strings there are two types of the "String" class in this object.
   * Better to be verbose instead of assuming the compiler will figure out what type you are
   * referring to.
   */

  ROSManager() {
    // no-op
  }

  @Override
  public <C extends JavaToMessageConverter> ROSNetworkPublisher<C> create(C converter) {
    return new ROSNetworkPublisher<>(converter);
  }

  /**
   * A node to publish GRIP messages with.
   */
  private static class GripPublisherNode extends AbstractNodeMain {
    private static final GraphName GRIP_ROOT = GraphName.of("GRIP/publisher");
    private final java.lang.String publishType;
    private final SinglePermitSemaphore semaphore = new SinglePermitSemaphore();
    private final GraphName nodeName;
    private volatile Optional<ROSMessagePublisher.Converter> publishMe = Optional.empty();

    public GripPublisherNode(java.lang.String publishType, GraphName nodeName) {
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
      // This CancellableLoop will be canceled automatically when the node shuts
      // down.
      final Publisher<Message> publisher = connectedNode.newPublisher(nodeName, publishType);
      connectedNode.executeCancellableLoop(new CancellableLoop() {

        @Override
        protected void loop() throws InterruptedException {
          semaphore.acquire();
          publishMe.ifPresent(publishAction -> {
            final Message message = publisher.newMessage();
            publishAction.convert(message, connectedNode.getTopicMessageFactory());
            publisher.publish(message);
          });
        }
      });
    }

    public void publish(ROSMessagePublisher.Converter o) {
      publishMe = Optional.of(o);
      semaphore.release();
    }
  }

  private static final class ROSNetworkPublisher<C extends JavaToMessageConverter> extends
      ROSMessagePublisher {
    private final C converter;
    private Optional<java.lang.String> name = Optional.empty();
    private Optional<GripPublisherExecutorPair> publisherExecutor = Optional.empty();

    protected ROSNetworkPublisher(C converter) {
      super();
      this.converter = checkNotNull(converter, "Converter cannot be null");
    }

    private GripPublisherNode createNewPublisher() {
      return new GripPublisherNode(converter.getType(), GraphName.of(name.get()));
    }

    @Override
    protected void publishNameChanged(Optional<java.lang.String> oldName, java.lang.String
        newName) {
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
      final GripPublisherNode gripPublisherNode = createNewPublisher();
      // Start the node running
      nodeMainExecutor.execute(gripPublisherNode, configuration);
      // Save the new instance of the publisher
      publisherExecutor = Optional.of(new GripPublisherExecutorPair(gripPublisherNode,
          nodeMainExecutor));
    }

    @Override
    public void publish(ROSMessagePublisher.Converter publish) {
      publisherExecutor.get().publisher.publish(publish);
    }

    @Override
    public void close() {
      publisherExecutor.ifPresent(pe -> pe.nodeMainExecutor.shutdown());
      publisherExecutor = Optional.empty();
    }

    /**
     * Defines a mapping between the publisher and the executor that is running the publisher.
     */
    private static class GripPublisherExecutorPair {
      private final GripPublisherNode publisher;
      private final NodeMainExecutor nodeMainExecutor;

      private GripPublisherExecutorPair(GripPublisherNode publisher, NodeMainExecutor
          nodeMainExecutor) {
        this.publisher = checkNotNull(publisher, "publisher cannot be null");
        this.nodeMainExecutor = checkNotNull(nodeMainExecutor, "nodeMainExecutor, cannot be null");
      }
    }
  }

}
