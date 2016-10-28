package edu.wpi.grip.core.sockets;

import com.google.common.eventbus.EventBus;

public class MockOutputSocket extends OutputSocketImpl<Boolean> {
  public MockOutputSocket(String name) {
    super(new EventBus(),
        SocketHints.Outputs.createBooleanSocketHint(name, false),
        "mock-output-" + name);
  }
}
