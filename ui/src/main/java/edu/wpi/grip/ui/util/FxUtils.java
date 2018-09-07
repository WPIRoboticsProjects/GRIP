package edu.wpi.grip.ui.util;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;

public final class FxUtils {

  private static final Logger logger = Logger.getLogger(FxUtils.class.getName());

  private FxUtils() {
    throw new UnsupportedOperationException("This is a utility class");
  }

  /**
   * Runs an action on the FX thread and blocks until it completes or throws an error.
   *
   * @param runnable the action to run
   */
  public static void runAndWait(Runnable runnable) {
    // Code taken verbatim from the now-inaccessible method
    // com.sun.javafx.application.PlatformImpl.runAndWait
    if (Platform.isFxApplicationThread()) {
      try {
        runnable.run();
      } catch (Throwable t) {
        logger.log(Level.WARNING, "Exception in runnable", t);
      }
    } else {
      final CountDownLatch doneLatch = new CountDownLatch(1);
      Platform.runLater(() -> {
        try {
          runnable.run();
        } finally {
          doneLatch.countDown();
        }
      });

      try {
        doneLatch.await();
      } catch (InterruptedException ex) {
        logger.log(Level.WARNING, "Interrupted while waiting for runnable to complete", ex);
        Thread.currentThread().interrupt();
      }
    }
  }

}
