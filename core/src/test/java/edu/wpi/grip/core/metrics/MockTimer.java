package edu.wpi.grip.core.metrics;

import com.google.common.eventbus.EventBus;

import java.util.function.Supplier;

public class MockTimer extends Timer {

  public static final MockTimer.Factory MOCK_FACTORY = simpleFactory(EventBus::new);

  public MockTimer(EventBus eventBus, Object source) {
    super(eventBus, source);
  }

  public static Timer.Factory simpleFactory(Supplier<EventBus> eventBusSupplier) {
    return source -> new MockTimer(eventBusSupplier.get(), source);
  }

  public static Timer.Factory simpleFactory(EventBus eventBus) {
    return source -> new MockTimer(eventBus, source);
  }

}
