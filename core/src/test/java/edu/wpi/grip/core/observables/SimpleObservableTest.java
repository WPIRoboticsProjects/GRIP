package edu.wpi.grip.core.observables;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test suite for {@link SimpleObservable}.
 */
@SuppressWarnings("unchecked")
public class SimpleObservableTest {

  private SimpleObservable observable;

  @Before
  public void setUp() {
    observable = new SimpleObservable();
  }

  @Test
  public void testConstructors() {
    assertNull(new SimpleObservable<>().get());
    Object val = new Object();
    assertEquals(val, new SimpleObservable<>(val).get());
  }

  @Test
  public void testSetNoChange() {
    observable.addObserver((prev, cur) -> fail("The value shouldn't have changed"));
    observable.set(null);
    assertNull(observable.get());
  }

  @Test
  public void testChange() {
    Object newValue = "something new";
    observable.addObserver((prev, cur) -> {
      assertNull(prev);
      assertEquals(newValue, cur);
    });
    observable.set(newValue);
    assertEquals(newValue, observable.get());
  }

  @Test
  public void testListeners() {
    boolean[] called = {false};
    Observer observer = (previous, current) -> called[0] = true;
    observable.addObserver(observer);
    observable.set(new Object());
    assertTrue(called[0]);

    called[0] = false; // reset
    observable.removeObserver(observer);
    assertFalse(called[0]);
  }

}
