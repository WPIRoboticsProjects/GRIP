package edu.wpi.grip.core.sockets;


import edu.wpi.grip.core.cuda.NullCudaDetector;

import com.google.common.eventbus.EventBus;

public class MockInputSocketFactory extends InputSocketImpl.FactoryImpl {

  public MockInputSocketFactory(EventBus eventBus) {
    super(eventBus, new NullCudaDetector());
  }
}
