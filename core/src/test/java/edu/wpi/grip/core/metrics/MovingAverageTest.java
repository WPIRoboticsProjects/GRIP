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
  public void testMoving() {
    MovingAverage a = new MovingAverage(2);
    double average;
    average = a.average(1);
    assertEquals("Average of {1} should be 1", 1, average, 0);
    average = a.average(2);
    assertEquals("Average of {1, 2} should be 1.5", 1.5, average, 0);
    average = a.average(3);
    assertEquals("Average of {2, 3} should be 2.5", 2.5, average, 0);
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
    assertEquals("Averagers were not identical", avg1, avg2, 0);
  }

}
