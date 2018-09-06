package edu.wpi.grip.ui.util;

import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;

public class FxUtils {

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
        System.err.println("Exception in runnable");
        t.printStackTrace();
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
        ex.printStackTrace();
      }
    }
  }

}
