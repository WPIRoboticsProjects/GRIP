package edu.wpi.grip.core.metrics;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Statistics analysis. Contains:
 * <ul>
 * <li>Number of samples ({@code n})</li>
 * <li>Mean value ({@code mean})</li>
 * <li>Standard deviation ({@code s})</li>
 * </ul>
 */
public final class Statistics {

  private final int n;
  private final double sum;
  private final double mean;
  private final double s;

  /**
   * Calculates the statistics of the given samples.
   *
   * @param samples the samples to analyze
   * @return a statistical analysis of the given samples
   */
  public static Statistics of(double... samples) {
    checkNotNull(samples);
    final int n = samples.length;
    double sum = 0;
    for (double d : samples) {
      sum += d;
    }
    final double mean = n == 0 ? 0 : sum / n;
    double variance = 0;
    for (double d : samples) {
      variance += ((d - mean) * (d - mean)) / n;
    }
    final double s = Math.sqrt(variance);
    return new Statistics(n, sum, mean, s);
  }

  private Statistics(int n, double sum, double mean, double s) {
    this.n = n;
    this.sum = sum;
    this.mean = mean;
    this.s = s;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("n", n)
        .add("mean", mean)
        .add("s", s)
        .toString();
  }

  /**
   * Gets the number of samples.
   */
  public int getN() {
    return n;
  }

  /**
   * Gets the sum of all the samples.
   */
  public double getSum() {
    return sum;
  }

  /**
   * Gets the arithmetic mean of the samples.
   */
  public double getMean() {
    return mean;
  }

  /**
   * Gets the standard deviation in the samples.
   */
  public double getStandardDeviation() {
    return s;
  }
}
