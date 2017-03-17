package edu.wpi.grip.core.metrics;

import com.google.common.eventbus.EventBus;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link Timer}.
 */
public class TimerTest {

  private static final String WRONG_TIME_MSG = "Elapsed time was wrong";
  private EventBus eventBus;

  @Before
  public void setUp() {
    eventBus = new EventBus();
  }

  @Test(expected = IllegalStateException.class)
  public void testStartedTwice() {
    Timer timer = new Timer(eventBus, this);
    timer.started();
    timer.started();
    fail("Timer should have thrown an exception");
  }

  @Test(expected = IllegalStateException.class)
  public void testStoppedWhenNotStarted() {
    Timer timer = new Timer(eventBus, this);
    timer.stop();
    fail("Timer should have thrown an exception");
  }

  @Test(expected = IllegalStateException.class)
  public void testStoppedTwice() {
    Timer timer = new Timer(eventBus, this);
    timer.started();
    timer.stop();
    timer.stop();
    fail("Timer should have thrown an exception");
  }

  @Test
  public void testTiming() {
    Timer timer = new MockTimer(eventBus, this);
    timer.started();
    timer.stop();
    assertEquals(WRONG_TIME_MSG, 1_000_000, timer.getElapsedTime());
  }

  @Test
  public void testReset() {
    Timer timer = new MockTimer(eventBus, this);
    timer.started();
    timer.reset();
    assertEquals("Elapsed time was not reset", 0, timer.getElapsedTime(), 0);

    timer.started();
    timer.stop();
    assertEquals(WRONG_TIME_MSG, 1_000_000, timer.getElapsedTime());
    timer.reset();
    assertEquals("Elapsed time was not reset", 0, timer.getElapsedTime(), 0);
  }

  @Test
  public void testTime() {
    Timer timer = new MockTimer(eventBus, this);
    AtomicBoolean ran = new AtomicBoolean(false);
    timer.time(() -> ran.set(true));
    assertEquals(WRONG_TIME_MSG, 1_000_000, timer.getElapsedTime());
    assertTrue("Did not run", ran.get());
  }

  @Test(expected = IllegalStateException.class)
  public void testTimeThrowsException() {
    Timer timer = new Timer(eventBus, this);
    timer.started();
    timer.time(() -> { });
    fail("An exception should have been thrown");
  }

}
