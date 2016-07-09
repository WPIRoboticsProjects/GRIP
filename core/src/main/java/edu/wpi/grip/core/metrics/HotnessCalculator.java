package edu.wpi.grip.core.metrics;

/**
 * Analyzes step and pipeline timings.
 */
public class HotnessCalculator {

  /**
   * Calculates the 'hotness' of the given value based on accrued statistics.
   *
   * @param statistics the accrued statistics to calculate the hotness from
   * @param value      the value to calculate the hotness of
   * @return the hotness of the given value.
   */
  public double hotness(Statistics statistics, double value) {
    if (value <= statistics.getMean()) {
      // Avoid negative hotness
      return 0;
    }
    return (value - statistics.getMean()) / statistics.getStandardDeviation();
  }

}
