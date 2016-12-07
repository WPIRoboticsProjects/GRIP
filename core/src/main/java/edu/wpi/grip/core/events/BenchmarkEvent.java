package edu.wpi.grip.core.events;

/**
 * An event posted before and after a pipeline is benchmarked.
 */
public final class BenchmarkEvent {

  private final boolean isStart;

  private BenchmarkEvent(boolean isStart) {
    this.isStart = isStart;
  }

  public static BenchmarkEvent started() {
    return new BenchmarkEvent(true);
  }

  public static BenchmarkEvent finished() {
    return new BenchmarkEvent(false);
  }

  public boolean isStart() {
    return isStart;
  }
}
