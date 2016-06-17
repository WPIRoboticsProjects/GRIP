package edu.wpi.grip.core.operations.network.ros;

public class MockROSManager implements ROSNetworkPublisherFactory {

  @Override
  public <C extends JavaToMessageConverter> ROSMessagePublisher create(C converter) {
    return null;
  }
}
