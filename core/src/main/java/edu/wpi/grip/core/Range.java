package edu.wpi.grip.core;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Holds the lower and upper bounds of a range of numbers.
 */
public class Range {

  private double min;
  private double max;

  /**
   * Creates a new range with both bounds equal to zero.
   */
  public Range() {
    this(0, 0);
  }

  /**
   * Creates a new range with the given bounds.
   *
   * @param min the lower end of the range
   * @param max the upper end of the range
   *
   * @throws IllegalArgumentException if min > max
   */
  public Range(double min, double max) {
    checkArgument(min <= max, "Min must be <= max");
    this.min = min;
    this.max = max;
  }

  /**
   * Creates a new range with the given bounds. This is equivalent to {@link new Range(min, max)}.
   *
   * @param min the lower end of the range
   * @param max the upper end of the range
   *
   * @return a new range for the given bounds
   *
   * @throws IllegalArgumentException if min > max
   */
  public static Range of(double min, double max) {
    return new Range(min, max);
  }

  /**
   * Sets the lower end of the range.
   *
   * @param min the new lower end of the range
   *
   * @throws IllegalArgumentException if min > max
   */
  public void setMin(double min) {
    checkArgument(min <= max, "Min must be <= max");
    this.min = min;
  }

  /**
   * Sets the upper end of the range.
   *
   * @param max the new upper end of the range
   *
   * @throws IllegalArgumentException if max < min
   */
  public void setMax(double max) {
    checkArgument(max >= min, "Max must be >= min");
    this.max = max;
  }

  /**
   * Gets the lower bound of the range.
   *
   * @return the lower bound of the range
   */
  public double getMin() {
    return min;
  }

  /**
   * Gets the upper bound of the range.
   *
   * @return the upper bound of the range
   */
  public double getMax() {
    return max;
  }

  public String toString() {
    return String.format("[%f, %f]", min, max);
  }

}
