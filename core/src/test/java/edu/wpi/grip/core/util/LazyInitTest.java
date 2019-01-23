package edu.wpi.grip.core.util;

import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class LazyInitTest {

  @Test
  public void testFactoryIsOnlyCalledOnce() {
    final String output = "foo";
    final int[] count = {0};
    final Supplier<String> factory = () -> {
      count[0]++;
      return output;
    };

    LazyInit<String> lazyInit = new LazyInit<>(factory);
    lazyInit.get();
    assertEquals(1, count[0]);

    lazyInit.get();
    assertEquals("Calling get() more than once should only call the factory once", 1, count[0]);
  }

  @Test
  public void testClear() {
    final String output = "foo";
    final int[] count = {0};
    final Supplier<String> factory = () -> {
      count[0]++;
      return output;
    };
    LazyInit<String> lazyInit = new LazyInit<>(factory);
    lazyInit.get();
    assertEquals(1, count[0]);

    lazyInit.clear();
    lazyInit.get();
    assertEquals(2, count[0]);
  }

}
