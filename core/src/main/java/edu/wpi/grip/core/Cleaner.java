package edu.wpi.grip.core;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * A cleaner schedules a best-effort GC call to periodically run to clean up accumulated objects.
 * This is mostly intended for JavaCV deallocator objects that don't get cleaned up after calling
 * {@link Pointer#deallocate()} or {@link Mat#release()}.
 */
public final class Cleaner {

  private static final Logger log = Logger.getLogger(Cleaner.class.getName());

  private volatile boolean started = false;

  /**
   * The singleton instance.
   */
  private static final Cleaner cleaner = new Cleaner();

  /**
   * Gets the cleaner instance.
   */
  public static Cleaner get() {
    return cleaner;
  }

  private Cleaner() {
    // Private constructor -- use get() to get the singleton instance
  }

  /**
   * Starts the cleaner. Only the first invocation of this method will have an effect.
   */
  public void start() {
    if (started) {
      return;
    }
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(this::runGc, 0, 15, TimeUnit.SECONDS);
    started = true;
  }

  /**
   * Pokes the GC to run and logs it.
   */
  @SuppressFBWarnings(value = "DM_GC",
                      justification = "GC is called infrequently")
  private void runGc() {
    System.gc();
    log.info("GC");
  }

}
