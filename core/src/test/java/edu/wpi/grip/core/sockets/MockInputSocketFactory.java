package edu.wpi.grip.core.sockets;


import com.fasterxml.uuid.Generators;
import com.google.common.eventbus.EventBus;

public class MockInputSocketFactory extends InputSocketImpl.FactoryImpl {

  public MockInputSocketFactory(EventBus eventBus) {
    super(() -> eventBus, Generators.timeBasedGenerator());
  }
}
