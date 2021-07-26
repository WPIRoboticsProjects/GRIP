package edu.wpi.grip.core.metrics;

import com.google.common.base.MoreObjects;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Statistics analysis. Contains:
 * <ul>
 * <li>Number of samples</li>
 * <li>Sum</li>
 * <li>Mean value</li>
 * <li>Standard deviation</li>
 * </ul>
 */
@Immutable
public final class Statistics {

  private final int numSamples;
  private final double sum;
  private final double mean;
  private final double standardDeviation;

  /**
   * "null" statistics with every value set to zero.
   */
  public static final Statistics NIL = new Statistics(0, 0, 0, 0);

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

  /**
   * Calculates the statistics of the given samples.
   *
   * @param samples the samples to analyze
   * @return a statistical analysis of the given samples
   */
  public static Statistics of(Collection<? extends Number> samples) {
    checkNotNull(samples);
    return of(samples.stream().mapToDouble(Number::doubleValue).toArray());
  }

  private Statistics(int numSamples, double sum, double mean, double standardDeviation) {
    this.numSamples = numSamples;
    this.sum = sum;
    this.mean = mean;
    this.standardDeviation = standardDeviation;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("numSamples", numSamples)
        .add("mean", mean)
        .add("standardDeviation", standardDeviation)
        .toString();
  }

  /**
   * Gets the number of samples.
   */
  public int getNumSamples() {
    return numSamples;
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
    return standardDeviation;
  }

  /**
   * Calculates the 'hotness' of the given value based on these statistics. Using a {@code value}
   * that is not in the data set used to create these statistics will most likely have a useless
   * result. Hotness is equal to the number of standard deviations above the mean, or zero if the
   * input value is not above the mean. Effectively, this means
   * {@code hotness(x) == max(0, ((x - mean) / standard deviation))}.
   *
   * <p>If this set of statistics was calculated from less than two data points, 'hotness' doesn't
   * mean anything and this method will always return zero.
   *
   * @param value the value to calculate the hotness of
   * @return the hotness of the given value.
   */
  public double hotness(double value) {
    if (numSamples < 2) {
      // Hotness doesn't make sense if there's 0 or 1 data points
      return 0;
    }
    if (value <= mean) {
      // Avoid negative hotness
      return 0;
    }
    return (value - mean) / standardDeviation;
  }

}
