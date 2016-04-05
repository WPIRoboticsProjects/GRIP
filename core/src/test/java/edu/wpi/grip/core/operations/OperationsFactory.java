package edu.wpi.grip.core.operations;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.MockMapNetworkPublisher;
import edu.wpi.grip.core.operations.network.ros.JavaToMessageConverter;
import edu.wpi.grip.core.operations.network.ros.ROSMessagePublisher;
import edu.wpi.grip.core.operations.network.ros.ROSNetworkPublisherFactory;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.MockInputSocketFactory;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.sockets.OutputSocket;

import java.util.Optional;

public class OperationsFactory {

    private static class MockROSMessagePublisher<C extends JavaToMessageConverter> extends ROSMessagePublisher {
        public MockROSMessagePublisher(C converter) {

        }

        @Override
        public void publish(ROSMessagePublisher.Converter publish) {

        }

        @Override
        protected void publishNameChanged(Optional<String> oldName, String newName) {

        }

        @Override
        public void close() {

        }
    }

    public static Operations create(EventBus eventBus) {

        return create(eventBus, MockMapNetworkPublisher::new, MockROSMessagePublisher::new,
                new MockInputSocketFactory(eventBus), new MockOutputSocketFactory(eventBus));
    }

    public static Operations create(EventBus eventBus,
                                    MapNetworkPublisherFactory mapFactory,
                                    ROSNetworkPublisherFactory rosFactory,
                                    InputSocket.Factory isf,
                                    OutputSocket.Factory osf) {
        return new Operations(eventBus, mapFactory, rosFactory, isf, osf);
    }

    public static CVOperations createCV(EventBus eventBus) {
        return new CVOperations(eventBus, new MockInputSocketFactory(eventBus), new MockOutputSocketFactory(eventBus));
    }
}
