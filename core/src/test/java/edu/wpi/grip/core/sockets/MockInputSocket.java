package edu.wpi.grip.core.sockets;


import com.google.common.eventbus.EventBus;

public class MockInputSocket extends InputSocketImpl<Boolean> {
    public MockInputSocket(String name) {
        super(new EventBus(), SocketHints.Outputs.createBooleanSocketHint(name, false));
    }
}
