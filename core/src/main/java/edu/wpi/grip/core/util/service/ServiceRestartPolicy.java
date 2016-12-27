package edu.wpi.grip.core.util.service;

/**
 * @see <a href="https://gist.github.com/vladdu/b8af7709e26206b1832b">Original version</a>
 */
@FunctionalInterface
public interface ServiceRestartPolicy {

  /**
   * A restart policy that has a service restart immediately.
   */
  ServiceRestartPolicy IMMEDIATE = () -> 0L;

  /**
   * Policy might want to keep track of when the latest restarts have happened.
   */
  default void notifyRestart() {
    /* no-op */
  }

  /**
   * When the service should restart, in nanoseconds from the point when the service failed. A value
   * less than or equal to zero means an immediate restart.
   */
  long restartDelay();

}
