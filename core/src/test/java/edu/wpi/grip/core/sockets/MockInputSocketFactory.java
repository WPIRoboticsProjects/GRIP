package edu.wpi.grip.core.sockets;


import com.google.common.eventbus.EventBus;

public class MockInputSocketFactory extends InputSocketImpl.FactoryImpl {

    public MockInputSocketFactory(EventBus eventBus) {
        super(eventBus);
    }
}
