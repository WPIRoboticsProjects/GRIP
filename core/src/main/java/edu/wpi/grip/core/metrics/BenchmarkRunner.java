package edu.wpi.grip.core.metrics;

import edu.wpi.grip.core.events.BenchmarkEvent;
import edu.wpi.grip.core.events.RunStartedEvent;
import edu.wpi.grip.core.events.RunStoppedEvent;
import edu.wpi.grip.core.events.StartSingleBenchmarkRunEvent;
import edu.wpi.grip.core.events.TimerEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Benchmark runner. This runs the pipeline multiple times to collect data about how long each
 * step takes to run.
 */
@Singleton
public class BenchmarkRunner {

  private final EventBus eventBus;

  /**
   * State flag.
   */
  private final AtomicBoolean isBenchmarking = new AtomicBoolean(false);

  /**
   * The number of runs left in the benchmark.
   */
  private final AtomicInteger runsRemaining = new AtomicInteger(0);

  /**
   * Keep track of timers so we can reset them after the benchmark finishes running.
   */
  private final AtomicReference<Set<Timer>> timers = new AtomicReference<>(new HashSet<>());

  @Inject
  BenchmarkRunner(EventBus eventBus) {
    this.eventBus = eventBus;
  }

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
    eventBus.post(new StartSingleBenchmarkRunEvent());
  }

  @Subscribe
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void onTimerEvent(TimerEvent event) {
    this.timers.get().add(event.getTimer());
  }

  @Subscribe
  @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter"})
  private void onRunStart(@Nullable RunStartedEvent event) {
    if (!isBenchmarking.get()) {
      return;
    }
    runsRemaining.decrementAndGet();
  }

  @Subscribe
  @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter"})
  private void onRunStop(@Nullable RunStoppedEvent event) {
    if (!isBenchmarking.get()) {
      return;
    }
    if (runsRemaining.get() == 0) {
      cleanUp();
      eventBus.post(BenchmarkEvent.finished());
    } else {
      eventBus.post(new StartSingleBenchmarkRunEvent());
    }
  }

  /**
   * Resets the runner if a "finished" benchmark event is posted while a benchmark is being run.
   * This is used to kill the runner if the analysis window is closed or the application is exited.
   */
  @Subscribe
  @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter"})
  private void onBenchmarkEvent(BenchmarkEvent event) {
    if (isBenchmarking.get() && !event.isStart()) {
      cleanUp();
    }
  }

  private void cleanUp() {
    isBenchmarking.set(false);
    runsRemaining.set(0);
    timers.get().forEach(Timer::reset);
    timers.get().clear();
  }

  /**
   * Checks if this benchmark runner is currently running a benchmark.
   *
   * @return true if a benchmark is running, false otherwise
   */
  public boolean isRunning() {
    return isBenchmarking.get();
  }

  /**
   * Checks how many runs are left in the benchmark. Returns zero if no benchmark is running.
   *
   * @return the number of runs left in the benchmark
   */
  public int getRunsRemaining() {
    return runsRemaining.get();
  }

}
