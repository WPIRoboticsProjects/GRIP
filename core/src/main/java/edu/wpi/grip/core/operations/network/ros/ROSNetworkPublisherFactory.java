package edu.wpi.grip.core.operations.network.ros;


/**
 * A factory that can be used to publish ROS messages.
 */
@FunctionalInterface
public interface ROSNetworkPublisherFactory {
  <C extends JavaToMessageConverter> ROSMessagePublisher create(C converter);
}
