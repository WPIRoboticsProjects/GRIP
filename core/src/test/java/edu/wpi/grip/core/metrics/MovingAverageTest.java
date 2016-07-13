package edu.wpi.grip.core.metrics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link MovingAverage}.
 */
public class MovingAverageTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeWindowSize() {
    new MovingAverage(-1);
    fail("Negative window size should error");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroWindowSize() {
    new MovingAverage(0);
    fail("Zero window size should error");
  }

  @Test
  public void testAverageSingle() {
    MovingAverage a = new MovingAverage(1);
    double value = 123;
    double average = a.average(value);
    assertEquals(value, average, 0);
  }

  @Test
  public void testAverageMulti() {
    MovingAverage a = new MovingAverage(5);
    double[] values = {1, 2, 3, 4, 5};
    double avg = 0;
    for (double d : values) {
      avg = a.average(d);
    }
    assertEquals(3, avg, 0);
  }

  @Test
  public void testMoving() {
    MovingAverage a = new MovingAverage(2);
    double average;
    a.average(1);
    average = a.average(2);
    assertEquals(1.5, average, 0);
    average = a.average(3);
    assertEquals(2.5, average, 0);
  }

  @Test
  public void testCopy() {
    MovingAverage a = new MovingAverage(3);
    MovingAverage copy = a.copy();
    double avg1 = 111;
    double avg2 = 222;
    for (int i = 0; i < 5; i++) {
      avg1 = a.average(i);
      avg2 = copy.average(i);
    }
    assertEquals(avg1, avg2, 0);
  }

}
