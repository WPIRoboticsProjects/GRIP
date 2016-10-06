package edu.wpi.grip.core.metrics;

import edu.wpi.grip.core.events.TimerEvent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Timer for code that gets run.
 *
 * <p>Sample usage:
 * <pre><code>
 *   Timer myTimer = ...
 *   myTimer.time(this::doSomething);
 * </code></pre>
 * </p>
 */
public class Timer {

  private final EventBus eventBus;
  private final Stopwatch stopwatch;

  private final Object target;
  private long elapsedTime = 0;

  @Inject
  Timer(EventBus eventBus, @Assisted Object target) {
    this(eventBus, target, Stopwatch.createUnstarted());
  }

  @VisibleForTesting
  Timer(EventBus eventBus, Object target, Stopwatch stopwatch) {
    this.eventBus = eventBus;
    this.stopwatch = stopwatch;
    this.target = checkNotNull(target, "target");
  }

  /**
   * Starts the timer. Call {@link #stop()} to stop timing.
   *
   * @throws IllegalStateException if this a call to this method is preceded by another call to
   *                               {@code started()}
   */
  public synchronized void started() {
    if (stopwatch.isRunning()) {
      throw new IllegalStateException("Already started");
    }
    stopwatch.reset().start();
  }

  /**
   * Stops the timer. This will post a {@link TimerEvent} containing the elapsed time
   * and analysis to the event bus.
   *
   * @throws IllegalStateException if this a call to this method is not preceded by a call to
   *                               {@link #started()}.
   */
  public synchronized void stop() {
    if (!stopwatch.isRunning()) {
      throw new IllegalStateException("Already stopped");
    }
    stopwatch.stop();
    this.elapsedTime = stopwatch.elapsed(TimeUnit.MICROSECONDS);
    eventBus.post(new TimerEvent(this, target, elapsedTime));
  }

  /**
   * Resets this timer.
   */
  public synchronized void reset() {
    if (stopwatch.isRunning()) {
      stopwatch.stop();
    }
    elapsedTime = 0;
  }

  /**
   * Times the given runnable target. This is safe, even if the target throws an exception.
   *
   * <p>Sample usage:
   * <pre><code>
   *   Timer myTimer = ...
   *   myTimer.time(this::doSomething);
   * </code></pre></p>
   *
   * @param target the code to time
   * @throws IllegalStateException if this timer is already timing something
   */
  public void time(Runnable target) {
    if (stopwatch.isRunning()) {
      throw new IllegalStateException("This timer is already timing something");
    }
    try {
      started();
      target.run();
    } finally {
      stop();
    }
  }

  /**
   * Gets the time elapsed between a call to {@link #started()} and a call to {@link #stop()},
   * in microseconds.
   */
  public long getElapsedTime() {
    return elapsedTime;
  }

  public interface Factory {
    Timer create(Object target);
  }

}
