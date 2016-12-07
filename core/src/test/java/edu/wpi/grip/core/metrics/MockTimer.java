package edu.wpi.grip.core.metrics;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.eventbus.EventBus;

import java.util.function.Supplier;

/**
 * Mock timer class. {@link #getElapsedTime()} will always return 1,000,000 µs (one second).
 */
public class MockTimer extends Timer {

  public static final MockTimer.Factory MOCK_FACTORY = simpleFactory(EventBus::new);

  public MockTimer(EventBus eventBus, Object source) {
    super(eventBus, source, Stopwatch.createUnstarted(new MockTicker()));
  }

  public static Timer.Factory simpleFactory(Supplier<EventBus> eventBusSupplier) {
    return source -> new MockTimer(eventBusSupplier.get(), source);
  }

  public static Timer.Factory simpleFactory(EventBus eventBus) {
    return source -> new MockTimer(eventBus, source);
  }

  /**
   * Ticker implementation that ticks by one second for each call to {@link #read()}.
   */
  public static final class MockTicker extends Ticker {

    private static final long tickSize = 1_000_000_000;
    private long tick = 0;

    @Override
    public long read() {
      tick += tickSize;
      return tick;
    }

  }

}
