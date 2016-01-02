package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;

public class MockOutputSocket extends OutputSocket<Boolean> {
    public MockOutputSocket(String socketName) {
        super(new EventBus(), SocketHints.Outputs.createBooleanSocketHint(socketName, false));
    }
}
