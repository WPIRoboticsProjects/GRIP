package edu.wpi.grip.core.sockets;


import com.google.common.eventbus.EventBus;

import java.util.UUID;

public class MockInputSocket extends InputSocketImpl<Boolean> {
  public MockInputSocket(String name) {
    super(EventBus::new,
        SocketHints.Outputs.createBooleanSocketHint(name, false),
        UUID.randomUUID());
  }
}
