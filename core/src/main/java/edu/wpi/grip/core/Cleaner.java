package edu.wpi.grip.core;

import edu.wpi.grip.core.events.RunStoppedEvent;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nullable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Cleans up unused objects by periodically calling {@link System#gc()} to nudge the
 * garbage collector to clean up dead native (JavaCV) objects. This is required because JavaCV
 * objects only free their native memory when they're garbage collected, so if they accumulate in
 * the heap, the app will use about 40x the memory as used heap (i.e. 230MB of used heap results in
 * about 9.8GB of used memory for the process). This is because {@code Mats} and anything
 * else extending {@link org.bytedeco.javacpp.Pointer} use native memory that greatly exceeds the
 * Java objects size on the heap.
 *
 * <p>JavaCV has a system property {@code org.bytedeco.javacpp.maxphysicalbytes} that it uses to
 * determine when to start deallocating native memory. However, this only results in calls to
 * {@code System.gc()} and imposes a hard upper limit on native memory use, limiting large images
 * or long pipelines. It's also not very portable: running from source needs it to be passed
 * as a JVM argument with gradle, and it can't be adjusted based on the amount of memory on the
 * system it's installed on. For us, manually running System.gc() periodically is a better solution.
 * </p>
 */
@Singleton
public class Cleaner {

  /**
   * The minimum time delay before running System.gc(). This is in milliseconds. A gc call will
   * never happen less than this amount of time after the previous call.
   */
  private static final long MIN_DELAY = 1000;

  /**
   * Stopwatch to keep track of the elapsed time since the last gc call.
   */
  private final Stopwatch stopwatch = Stopwatch.createUnstarted();

  /**
   * The minimum number of runs allowed before calling System.gc().
   */
  private static final int MIN_RUNS_BEFORE_GC = 5;

  /**
   * The number of runs since the last gc call.
   */
  private int runsSinceLastGc = 0;

  @Subscribe
  @SuppressFBWarnings(value = "DM_GC", justification = "GC is called infrequently")
  public void onRunFinished(@Nullable RunStoppedEvent e) {
    runsSinceLastGc++;
    if (!stopwatch.isRunning()) {
      stopwatch.start();
    }
    if (runsSinceLastGc >= MIN_RUNS_BEFORE_GC && stopwatch.elapsed(MILLISECONDS) >= MIN_DELAY) {
      runsSinceLastGc = 0;
      stopwatch.reset();
      System.gc();
    }
  }

}
