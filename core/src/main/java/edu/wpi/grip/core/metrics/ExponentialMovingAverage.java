package edu.wpi.grip.core.metrics;

import com.google.common.base.MoreObjects;

import javax.annotation.CheckReturnValue;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Exponential moving average filter.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Exponential_smoothing">Wikipedia</a>
 */
class ExponentialMovingAverage {

  private boolean started = false;
  private double lastValue = 0;
  private final double alpha;

  /**
   * Creates a new exponential moving average filter with the given alpha value
   *
   * @param alpha the smoothing factor used. Must be in the range (0, 1).
   */
  ExponentialMovingAverage(double alpha) {
    checkArgument(0 <= alpha && alpha <= 1, "0 ≤ alpha ≤ 1");
    this.alpha = alpha;
  }

  /**
   * Calculates the average based on the previous average and the next value in the sequence.
   *
   * @param nextValue the next value in the sequence to average
   * @return the new average value
   */
  @CheckReturnValue
  double average(double nextValue) {
    if (!started) {
      lastValue = nextValue;
      started = true;
      return lastValue;
    }
    lastValue = lastValue + alpha * (nextValue - lastValue);
    return lastValue;
  }

  /**
   * Resets the filter.
   */
  public void reset() {
    started = false;
    lastValue = 0;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("alpha", alpha)
        .add("value", lastValue)
        .add("started", started)
        .toString();
  }
}
