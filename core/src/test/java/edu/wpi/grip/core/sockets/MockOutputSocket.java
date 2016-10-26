package edu.wpi.grip.core.sockets;

import com.fasterxml.uuid.Generators;
import com.google.common.eventbus.EventBus;

public class MockOutputSocket extends OutputSocketImpl<Boolean> {
  public MockOutputSocket(String socketName) {
    super(EventBus::new,
        SocketHints.Outputs.createBooleanSocketHint(socketName, false),
        Generators.timeBasedGenerator().generate());
  }
}
