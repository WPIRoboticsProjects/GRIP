package edu.wpi.grip.core.metrics;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class AnalysisTest {

  @Test
  public void testArrayConstructor() {
    double[] values = {1, 2, 3, 4, 5};
    Analysis a = Analysis.of(values);
    assertEquals("Average should be 3", 3, a.getAverage(), 0);
    Statistics statistics = a.getStatistics();
    assertEquals("n should be 5", 5, statistics.getN(), 0);
    assertEquals("sum should be 15", 15, statistics.getSum(), 0);
    assertEquals("mean should be 3", 3, statistics.getMean(), 0);
    assertEquals("std dev should be sqrt(2)", Math.sqrt(2), statistics.getStandardDeviation(), 0);
    assertEquals("hotness should be 0", statistics.hotness(1), 0, 0);
    assertEquals("hotness should be 0", statistics.hotness(2), 0, 0);
    assertEquals("hotness should be 0", statistics.hotness(3), 0, 0);
    assertEquals("hotness should be 1/sqrt(2)", statistics.hotness(4), 1 / Math.sqrt(2), 0);
    assertEquals("hotness should be 2/sqrt(2)", statistics.hotness(5), 2 / Math.sqrt(2), 0);
  }

  @Test
  public void testCollectionConstructor() {
    Collection<? extends Number> values = Arrays.asList(1, 2, 3, 4, 5);
    Analysis a = Analysis.of(values);
    assertEquals("Average should be 3", 3, a.getAverage(), 0);
    Statistics statistics = a.getStatistics();
    assertEquals("n should be 5", 5, statistics.getN(), 0);
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
