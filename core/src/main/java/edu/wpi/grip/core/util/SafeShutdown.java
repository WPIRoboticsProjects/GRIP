package edu.wpi.grip.core.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * This class should be used to shutdown GRIP safely. This is because shutdown hooks may throw
 * exceptions, as such we need to know if the application being is shutdown.
 */
public final class SafeShutdown {

  public static volatile boolean stopping = false;

  static {
    /*
     * Shutdown hook run order is non-deterministic but this increases our likelihood of
     * flagging a shutdown
     * that we can't control.
     */

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
          justification = "Static variable is volatile")
      public void run() {
        SafeShutdown.stopping = true;
      }
    });
  }

  private SafeShutdown() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Shutdown's the VM in such a way that flags that the vm is stopping. This is so that we don't
   * run the normal exception handling code when shutting down the application.
   *
   * @param statusCode exit status.
   * @param hook       The hook to run before the System shutdown. This will be run after stopping
   *                   has been flagged true. This is nullable.
   * @see System#exit(int)
   */
  public static void exit(ExitCode statusCode, @Nullable PreSystemExitHook hook) {
    flagStopping();
    try {
      if (hook != null) {
        hook.run();
      }
    } finally {
      Logger.getLogger(SafeShutdown.class.getName()).info("Exiting GRIP");
      System.exit(statusCode.getCode());
    }

  }

  /**
   * Helper method that passes null as the PreSystemExitHook.
   *
   * @param statusCode exit status.
   * @see #exit(ExitCode, PreSystemExitHook)
   */
  public static void exit(ExitCode statusCode) {
    exit(statusCode, null);
  }

  /**
   * HACK! Shutdown hooks can throw exceptions. On Windows, the static method after {@link
   * org.bytedeco.javacpp.Loader#loadLibrary} throws such an exception in a shutdown hook.
   *
   * @return True if if the application is shutting down.
   * @see <a href="https://github.com/WPIRoboticsProjects/GRIP/issues/297">GRIP Issue</a>
   * @see <a href="https://github.com/bytedeco/javacpp/issues/60">Bytedeco issue</a>
   */
  public static boolean isStopping() {
    return stopping;
  }

  /**
   * Flags that the application is shutting down.
   */
  public static void flagStopping() {
    stopping = true;
  }

  /**
   * Exit codes used by the GRIP application.
   */
  public enum ExitCode {
    /**
     * Clean shutdown.
     */
    SAFE_SHUTDOWN(0x00),
    /**
     * An unknown exception was thrown and uncaught.
     */
    MISC_ERROR(0x01),

    /**
     * The HTTP server cannot start (typically due to the port already being in use).
     */
    HTTP_SERVER_COULD_NOT_START(0x02),
    /**
     * CUDA is required by OpenCV but no compatible runtime is available on the system.
     */
    CUDA_UNAVAILABLE(0x04),
    ;

    private final int code;

    ExitCode(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }

  @FunctionalInterface
  public interface PreSystemExitHook {
    void run();
  }
}
