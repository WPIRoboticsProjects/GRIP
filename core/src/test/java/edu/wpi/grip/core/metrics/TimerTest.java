package edu.wpi.grip.core.metrics;

import edu.wpi.grip.core.events.TimerEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests for {@link Timer}.
 */
public class TimerTest {

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
    timer.stopped();
    fail("Timer should have thrown an exception");
  }

  @Test(expected = IllegalStateException.class)
  public void testStoppedTwice() {
    Timer timer = new Timer(eventBus, this);
    timer.started();
    timer.stopped();
    timer.stopped();
    fail("Timer should have thrown an exception");
  }

  @Test
  public void testTiming() throws InterruptedException {
    Timer timer = new Timer(eventBus, this);
    timer.started();
    Thread.sleep(1000);
    timer.stopped();
    assertEquals("Elapsed time was out of tolerance", 1_000_000, timer.getElapsedTime(), 50_000);
  }

  @Test
  public void testAnalysisData() throws InterruptedException {
    Timer timer = new Timer(eventBus, this);
    ThreadLocal<TimerEvent> timerEvent = new ThreadLocal<>();
    eventBus.register(new Object() {
      @Subscribe
      private void onTimerEvent(TimerEvent event) {
        timerEvent.set(event);
      }
    });
    timer.started();
    Thread.sleep(1000);
    timer.stopped();
    assertNotNull("No TimerEvent received", timerEvent.get());
    TimerEvent event = timerEvent.get();
    assertEquals("Event had an unexpected target", this, event.getTarget());
    assertEquals("Elapsed time was out of tolerance", 1_000_000, event.getElapsedTime(), 50_000);
    Analysis analysis = event.getData();
    Statistics statistics = analysis.getStatistics();
    assertEquals("Analysis did not have 1 element", 1, analysis.getN());
    assertEquals("Average time was wrong", event.getElapsedTime(), analysis.getAverage(), 0);
    assertEquals("Statistics N was wrong", 1, statistics.getN());
    assertEquals("Statistics mean was wrong", event.getElapsedTime(), statistics.getMean(), 0);
    assertEquals("Statistics stddev was wrong", 0, statistics.getStandardDeviation(), 0);
    assertEquals("Statistics sum was wrong", event.getElapsedTime(), statistics.getSum(), 0);
  }

  @Test
  public void testReset() throws InterruptedException {
    Timer timer = new Timer(eventBus, this);
    timer.started();
    Thread.sleep(1000);
    timer.reset();
    assertEquals("Elapsed time was not reset", 0, timer.getElapsedTime(), 0);

    timer.started();
    Thread.sleep(1000);
    timer.stopped();
    assertEquals("Elapsed time was out of tolerance", 1_000_000, timer.getElapsedTime(), 50_000);
    timer.reset();
    assertEquals("Elapsed time was not reset", 0, timer.getElapsedTime(), 0);
  }

}
