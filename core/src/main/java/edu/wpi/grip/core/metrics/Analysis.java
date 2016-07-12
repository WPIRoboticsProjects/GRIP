package edu.wpi.grip.core.metrics;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Analysis data of a timed action. Contains
 * <ul>
 * <li>The number of samples taken</li>
 * <li>The average of the samples taken. This may not be an arithmetic mean, or even an average
 * of <i>all</i> the samples</li>
 * <li>Statistical analysis of the data.</li>
 * </ul>
 */
public final class Analysis {

  private static final int windowSize = 10;

  private final MovingAverage movingAverager = new MovingAverage(windowSize);

  private final List<Double> samples = new ArrayList<>();

  // The average time taken (exponential moving average)
  private double average = 0;

  // The number of samples taken
  private int n = 0;

  private Statistics statistics;

  /**
   * Updates this data with the given run time.
   *
   * @param nextValue a new data point to record
   */
  public void add(double nextValue) {
    samples.add(nextValue);
    average = movingAverager.average(nextValue);
    n++;
    statistics = Statistics.of(samples.stream().mapToDouble(Double::doubleValue).toArray());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("n", n)
        .add("average", average)
        .add("statistics", statistics)
        .toString();
  }

  /**
   * Resets the data.
   */
  public void reset() {
    samples.clear();
    movingAverager.reset();
    n = 0;
    average = 0;
    statistics = Statistics.NIL;
  }

  /**
   * Gets the average running time of the step this data is for.
   */
  public double getAverage() {
    return average;
  }

  /**
   * Gets the statistical analysis of the data.
   */
  public Statistics getStatistics() {
    return statistics;
  }

  /**
   * Gets the number of samples taken.
   */
  public int getN() {
    return n;
  }

}
