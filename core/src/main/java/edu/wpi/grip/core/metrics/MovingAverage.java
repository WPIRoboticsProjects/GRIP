package edu.wpi.grip.core.metrics;

import java.util.Deque;
import java.util.LinkedList;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Simple moving average filter.
 */
class MovingAverage {

  private final int windowSize;
  private int numSamples;
  private final Deque<Double> samples; // FIFO sample queue

  /**
   * Creates a new moving average filter with the given window size. It will only average the most
   * recent {@code windowSize} samples.
   *
   * @param windowSize the window size used. Must be a positive number.
   */
  MovingAverage(@Nonnegative int windowSize) {
    checkArgument(0 < windowSize, "0 < windowSize (given " + windowSize + ")");
    this.windowSize = windowSize;
    this.numSamples = 0;
    this.samples = new LinkedList<>();
  }

  /**
   * Calculates the average based on the previous average and the next value in the sequence.
   *
   * @param nextValue the next value in the sequence to average
   * @return the new average value
   */
  @CheckReturnValue
  double average(double nextValue) {
    // shift to make space for new value
    if (numSamples < windowSize) {
      // Don't have enough samples yet, so don't remove the oldest value
      numSamples++;
    } else {
      // Maxed out numSamples, remove oldest value
      samples.removeFirst();
    }
    samples.addLast(nextValue);
    return samples.stream()
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(0);
  }

  /**
   * Resets the filter.
   */
  public void reset() {
    samples.clear();
    numSamples = 0;
  }

}
