package edu.wpi.grip.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link Range}.
 */
public class RangeTest {

  @Test
  public void testDefaultConstructor() {
    Range r = new Range();
    assertEquals("Default min is not zero", 0, r.getMin(), 0);
    assertEquals("Default max is not zero", 0, r.getMax(), 0);
  }

  @Test
  public void testArgsConstructor() {
    Range r = new Range(-1, 1);
    assertEquals("Min was not -1", -1, r.getMin(), 0);
    assertEquals("Max was not 1", 1, r.getMax(), 0);
  }

  @Test
  public void testStaticConstructor() {
    Range r = Range.of(10, 20);
    assertEquals("Min was not 10", 10, r.getMin(), 0);
    assertEquals("Max was not 20", 20, r.getMax(), 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMinGreaterThanMax() {
    new Range(1, -1);
    fail("Exception should have been thrown if min > max");
  }

  @Test
  public void testSetMin() {
    Range r = new Range();
    r.setMin(-1);
    assertEquals("Min was not set correctly", -1, r.getMin(), 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMinGreaterThanMax() {
    Range r = new Range();
    r.setMin(1);
  }

  @Test
  public void testSetMax() {
    Range r = new Range();
    r.setMax(1);
    assertEquals("Max was not set correctly", 1, r.getMax(), 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMaxLessThanMin() {
    Range r = new Range();
    r.setMax(-1);
  }

}
