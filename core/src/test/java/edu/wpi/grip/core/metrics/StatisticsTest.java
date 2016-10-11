package edu.wpi.grip.core.metrics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatisticsTest {

  @Test
  public void testNil() {
    Statistics statistics = Statistics.NIL;
    assertEquals("n should be 0", 0, statistics.getNumSamples(), 0);
    assertEquals("sum should be 0", 0, statistics.getSum(), 0);
    assertEquals("mean should be 0", 0, statistics.getMean(), 0);
    assertEquals("std dev should be 0", 0, statistics.getStandardDeviation(), 0);
    assertEquals("hotness should be 0", statistics.hotness(7), 0, 0);
  }

  @Test
  public void testEmpty() {
    double[] values = {};
    Statistics statistics = Statistics.of(values);
    assertEquals("n should be 0", 0, statistics.getNumSamples(), 0);
    assertEquals("sum should be 0", 0, statistics.getSum(), 0);
    assertEquals("mean should be 0", 0, statistics.getMean(), 0);
    assertEquals("std dev should be 0", 0, statistics.getStandardDeviation(), 0);
    assertEquals("hotness should be 0", statistics.hotness(15), 0, 0);
  }

  @Test
  public void testSingleDataPoint() {
    double[] values = {Math.PI};
    Statistics statistics = Statistics.of(values);
    assertEquals("n should be 1", 1, statistics.getNumSamples(), 0);
    assertEquals("sum should be pi", Math.PI, statistics.getSum(), 0);
    assertEquals("mean should be pi", Math.PI, statistics.getMean(), 0);
    assertEquals("std dev should be 0", 0, statistics.getStandardDeviation(), 0);
    assertEquals("hotness should be 0", statistics.hotness(31), 0, 0);
  }

  @Test
  public void testMultipleDataPoints() {
    double[] values = {1, 2, 3, 4, 5};
    Statistics statistics = Statistics.of(values);
    assertEquals("n should be 5", 5, statistics.getNumSamples(), 0);
    assertEquals("sum should be 15", 15, statistics.getSum(), 0);
    assertEquals("mean should be 3", 3, statistics.getMean(), 0);
    assertEquals("std dev should be sqrt(2)", Math.sqrt(2), statistics.getStandardDeviation(), 0);
    assertEquals("hotness should be 0", statistics.hotness(1), 0, 0);
    assertEquals("hotness should be 0", statistics.hotness(2), 0, 0);
    assertEquals("hotness should be 0", statistics.hotness(3), 0, 0);
    assertEquals("hotness should be 1/sqrt(2)", statistics.hotness(4), 1 / Math.sqrt(2), 0);
    assertEquals("hotness should be 2/sqrt(2)", statistics.hotness(5), 2 / Math.sqrt(2), 0);
  }

}
