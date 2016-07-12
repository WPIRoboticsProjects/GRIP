package edu.wpi.grip.core.metrics;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

/**
 * Analysis data of a timed action. Contains
 * <ul>
 * <li>The number of samples taken</li>
 * <li>The average of the samples taken. This may not be an arithmetic mean, or even an average
 * of <i>all</i> the samples</li>
 * <li>Statistical analysis of the data.</li>
 * </ul>
 */
@Immutable
public final class Analysis {

  private static final int windowSize = 10;

  private MovingAverage movingAverager;
  private final List<Double> samples;
  private double average;
  private int n;
  private Statistics statistics;

  /**
   * Creates a new analysis with no data. Use {@link #add(double) add} to get new analysis data
   * based off this one.
   *
   * <p>Sample use:
   * <pre><code>
   *   Analysis myAnalysis = new Analysis();
   *   ...
   *   while (hasMoreData()) {
   *     myAnalysis = myAnalysis.add(getNextDataPoint());
   *   }
   * </code></pre>
   * </p>
   */
  public Analysis() {
    this.movingAverager = new MovingAverage(windowSize);
    this.samples = new ArrayList<>();
    this.average = 0;
    this.statistics = Statistics.NIL;
  }

  /**
   * Updates this data with the given run time.
   *
   * @param nextValue a new data point to record
   */
  @CheckReturnValue
  public Analysis add(double nextValue) {
    Analysis result = new Analysis();
    result.samples.addAll(this.samples);
    result.samples.add(nextValue);
    result.n = this.n + 1;
    result.movingAverager = this.movingAverager.copy();
    result.average = result.movingAverager.average(nextValue);
    result.statistics = Statistics.of(result.samples
        .stream()
        .mapToDouble(d -> d)
        .toArray());
    return result;
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
