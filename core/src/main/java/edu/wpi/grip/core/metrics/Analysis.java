package edu.wpi.grip.core.metrics;

import com.google.common.base.MoreObjects;

import java.util.Collection;
import java.util.stream.DoubleStream;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Analysis data of a timed action. Contains
 * <ul>
 * <li>The average of the samples taken.</li>
 * <li>A statistical analysis of the data.</li>
 * </ul>
 */
@Immutable
public final class Analysis {

  private final double average;
  private final Statistics statistics;

  /**
   * 'null' analysis.
   */
  public static Analysis NIL = Analysis.of();

  /**
   * Class-private constructor. Use {@link #of(double...)} or {@link #of(Collection)}
   * factory methods.
   */
  private Analysis(double average, Statistics statistics) {
    this.average = average;
    this.statistics = statistics;
  }

  /**
   * Analyzes the given values.
   *
   * @param values the values to analyze
   * @return an analysis of the given values
   */
  public static Analysis of(double... values) {
    checkNotNull(values, "values");
    double average = DoubleStream.of(values).average().orElse(0);
    Statistics statistics = Statistics.of(values);
    return new Analysis(average, statistics);
  }

  /**
   * Analyzes the given collection of values.
   *
   * @param values the values to analyze
   * @return an analysis of the given values
   */
  public static Analysis of(Collection<? extends Number> values) {
    checkNotNull(values, "values");
    return of(values.stream().mapToDouble(Number::doubleValue).toArray());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
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

}
