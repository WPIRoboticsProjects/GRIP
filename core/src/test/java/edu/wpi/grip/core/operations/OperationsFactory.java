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
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.util.MockFileManager;

import com.google.common.eventbus.EventBus;

import java.util.Optional;

public class OperationsFactory {

  public static Operations create(EventBus eventBus) {
    return create(eventBus,
        MockMapNetworkPublisher::new,
        MockMapNetworkPublisher::new,
        MockROSMessagePublisher::new,
        new MockFileManager(),
        new MockInputSocketFactory(eventBus),
        new MockOutputSocketFactory(eventBus));
  }

  public static Operations create(EventBus eventBus,
                                  MapNetworkPublisherFactory mapFactory,
                                  MapNetworkPublisherFactory httpFactory,
                                  ROSNetworkPublisherFactory rosFactory,
                                  FileManager fileManager,
                                  InputSocket.Factory isf,
                                  OutputSocket.Factory osf) {
    return new Operations(eventBus, mapFactory, httpFactory, rosFactory, fileManager, isf, osf);
  }

  public static CVOperations createCV(EventBus eventBus) {
    return new CVOperations(eventBus, new MockInputSocketFactory(eventBus), new
        MockOutputSocketFactory(eventBus));
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
