package edu.wpi.grip.core.metrics;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.BenchmarkEvent;
import edu.wpi.grip.core.events.BenchmarkRunEvent;
import edu.wpi.grip.core.events.RunStartedEvent;
import edu.wpi.grip.core.events.RunStoppedEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Benchmark runner.
 */
@Singleton
public class BenchmarkRunner {

  @Inject
  private EventBus eventBus;
  @Inject
  private Pipeline pipeline;

  private AtomicBoolean isBenchmarking = new AtomicBoolean(false);
  private AtomicInteger runsRemaining = new AtomicInteger(0);

  /**
   * Benchmarks the pipeline.
   *
   * @param numRuns the number of runs of the pipeline to collect events from.
   */
  public void run(int numRuns) {
    checkArgument(numRuns > 0, "Must run the pipeline at least once");
    this.isBenchmarking.set(true);
    this.runsRemaining.set(numRuns);
    eventBus.post(BenchmarkEvent.started());
    eventBus.post(new BenchmarkRunEvent());
  }

  @Subscribe
  private void onRunStart(@Nullable RunStartedEvent event) {
    if (!isBenchmarking.get()) {
      return;
    }
    runsRemaining.decrementAndGet();
  }

  @Subscribe
  private void onRunStop(@Nullable RunStoppedEvent event) {
    if (!isBenchmarking.get()) {
      return;
    }
    if (runsRemaining.get() == 0) {
      isBenchmarking.set(false);
      eventBus.post(BenchmarkEvent.finished());
    } else {
      eventBus.post(new BenchmarkRunEvent());
    }
  }

  @Subscribe
  private void onBenchmarkEvent(BenchmarkEvent event) {
    if (isBenchmarking.get() && !event.isStart()) {
      isBenchmarking.set(false);
      runsRemaining.set(0);
    }
  }

}
