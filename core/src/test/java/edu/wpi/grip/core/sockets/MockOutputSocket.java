package edu.wpi.grip.core.sockets;

import com.google.common.eventbus.EventBus;

public class MockOutputSocket extends OutputSocketImpl<Boolean> {
    public MockOutputSocket(String socketName) {
        super(new EventBus(), SocketHints.Outputs.createBooleanSocketHint(socketName, false));
    }
}
