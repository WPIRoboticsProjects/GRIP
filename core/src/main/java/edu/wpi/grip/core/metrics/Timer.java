package edu.wpi.grip.core.metrics;

import edu.wpi.grip.core.events.TimerEvent;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Witness to methods or functions that get run.
 *
 * <p>Usage:
 * <pre><code>
 *   Timer myTimer = ...
 *
 *   void method() {
 *     try {
 *       myTimer.started();
 *       // Do something
 *     } finally {
 *       myTimer.stopped();
 *     }
 *   }
 * </code></pre>
 * </p>
 */
public class Timer {

  private final EventBus eventBus;
  private final Stopwatch stopwatch = Stopwatch.createUnstarted();
  private boolean started = false;

  private final Object source;
  private final Analysis data;
  private long elapsedTime = 0;

  @Inject
  Timer(EventBus eventBus, @Assisted Object source) {
    this.eventBus = eventBus;
    this.source = checkNotNull(source, "source");
    this.data = new Analysis();
  }

  /**
   * Flags the witness as started. Call {@link #stopped()} to stop timing.
   *
   * @throws IllegalStateException if this a call to this method is not preceded by a call to
   *                               {@link #stopped()}.
   */
  public synchronized void started() {
    if (started) {
      throw new IllegalStateException("Already started");
    }
    stopwatch.reset().start();
    started = true;
  }

  /**
   * Flags the witness as stopped.
   *
   * @throws IllegalStateException if this a call to this method is not preceded by a call to
   *                               {@link #started()}.
   */
  public synchronized void stopped() {
    if (!started) {
      throw new IllegalStateException("Already stopped");
    }
    stopwatch.stop();
    this.elapsedTime = stopwatch.elapsed(TimeUnit.MICROSECONDS);
    data.add(elapsedTime);
    eventBus.post(new TimerEvent<>(source, elapsedTime, data));
    started = false;
  }

  /**
   * Gets the time elapsed between a call to {@link #started()} and a call to {@link #stopped()},
   * in microseconds.
   */
  public double getElapsedTime() {
    return elapsedTime;
  }

  public interface Factory {
    Timer create(Object source);
  }

}
