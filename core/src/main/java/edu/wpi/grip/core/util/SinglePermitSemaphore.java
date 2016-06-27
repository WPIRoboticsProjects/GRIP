package edu.wpi.grip.core.util;


import java.util.concurrent.Semaphore;

/**
 * A semaphore that behaves as if there was exactly one permit that can be acquired.
 */
public class SinglePermitSemaphore {
  private final Semaphore semaphore = new Semaphore(0);

  /**
   * Acquires the permit.
   */
  public void acquire() throws InterruptedException {
    // Acquire the first this one should permit if there is at least one permit
    semaphore.acquire();
    // Acquire the rest of the permits from the flag
    // Every time release is called another permit is added.
    // We need to clean up any old permits that we may have been given.
    semaphore.acquire(
        Math.max(0, semaphore.availablePermits()));
  }

  /**
   * Releases a permit.
   */
  public void release() {
    semaphore.release();
  }
}
