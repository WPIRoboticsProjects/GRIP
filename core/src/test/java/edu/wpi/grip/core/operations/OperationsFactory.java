package edu.wpi.grip.core.operations;


import edu.wpi.grip.core.FileManager;
import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.MockMapNetworkPublisher;
import edu.wpi.grip.core.operations.network.ros.JavaToMessageConverter;
import edu.wpi.grip.core.operations.network.ros.ROSMessagePublisher;
import edu.wpi.grip.core.operations.network.ros.ROSNetworkPublisherFactory;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.MockInputSocketFactory;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.util.MockFileManager;

import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;

import java.util.Optional;

public final class OperationsFactory {

  private OperationsFactory() {
    throw new UnsupportedOperationException(
        "This is a utility class. What do you expect to happen?");
  }

  public static Operations create(EventBus eventBus, Injector injector) {
    return create(eventBus,
        MockMapNetworkPublisher::new,
        MockMapNetworkPublisher::new,
        MockROSMessagePublisher::new,
        injector,
        new MockFileManager(),
        new MockInputSocketFactory(eventBus));
  }

  public static Operations create(EventBus eventBus,
                                  MapNetworkPublisherFactory mapFactory,
                                  MapNetworkPublisherFactory httpFactory,
                                  ROSNetworkPublisherFactory rosFactory,
                                  Injector injector,
                                  FileManager fileManager,
                                  InputSocket.Factory isf) {
    return new Operations(eventBus, mapFactory, httpFactory, rosFactory,
        injector, fileManager, isf);
  }

  public static CVOperations createCV(EventBus eventBus) {
    return new CVOperations(eventBus, new MockInputSocketFactory(eventBus), new
        MockOutputSocketFactory(eventBus));
  }

  private static class MockROSMessagePublisher<C extends JavaToMessageConverter>
      extends ROSMessagePublisher {
    @SuppressWarnings("PMD.UnusedFormalParameter") // Used to make the args list neater
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
