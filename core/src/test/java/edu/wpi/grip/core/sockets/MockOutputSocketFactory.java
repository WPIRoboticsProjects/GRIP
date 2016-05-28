package edu.wpi.grip.core.sockets;

import com.google.common.eventbus.EventBus;

public class MockOutputSocketFactory extends OutputSocketImpl.FactoryImpl {
    public MockOutputSocketFactory(EventBus eventBus) {
        super(eventBus);
    }
}
