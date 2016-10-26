package edu.wpi.grip.core.operations;


import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.MockMapNetworkPublisher;
import edu.wpi.grip.core.operations.network.ros.JavaToMessageConverter;
import edu.wpi.grip.core.operations.network.ros.ROSMessagePublisher;
import edu.wpi.grip.core.operations.network.ros.ROSNetworkPublisherFactory;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.MockInputSocketFactory;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.util.MockFileManager;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import java.util.Optional;

public class OperationsFactory {

  public static Operations create(EventBus eventBus) {
    return () ->
        ImmutableList.<OperationMetaData>builder()
            .addAll(createBasic(eventBus).operations())
            .addAll(createNetwork(eventBus).operations())
            .addAll(createCV(eventBus).operations())
            .build();
  }

  public static NetworkOperations createNetwork(EventBus eventBus) {
    return createNetwork(
        MockMapNetworkPublisher::new,
        MockMapNetworkPublisher::new,
        MockROSMessagePublisher::new,
        new MockInputSocketFactory(eventBus));
  }

  public static NetworkOperations createNetwork(MapNetworkPublisherFactory mapFactory,
                                                MapNetworkPublisherFactory httpFactory,
                                                ROSNetworkPublisherFactory rosFactory,
                                                InputSocket.Factory isf) {
    return new NetworkOperations(mapFactory, httpFactory, rosFactory, isf);
  }

  public static BasicOperations createBasic(EventBus eventBus) {
    return new BasicOperations(
        new MockFileManager(),
        new MockInputSocketFactory(eventBus),
        new MockOutputSocketFactory(eventBus));
  }

  public static CVOperations createCV(EventBus eventBus) {
    return new CVOperations(
        new MockInputSocketFactory(eventBus),
        new MockOutputSocketFactory(eventBus));
  }

  private static class MockROSMessagePublisher<C extends JavaToMessageConverter> extends
      ROSMessagePublisher {
    public MockROSMessagePublisher(C converter) {

    }

    @Override
    @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
    public void publish(ROSMessagePublisher.Converter publish) {

    }

    @Override
    @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
    protected void publishNameChanged(Optional<String> oldName, String newName) {

    }

    @Override
    @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
    public void close() {

    }
  }
}
