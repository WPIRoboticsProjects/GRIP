package edu.wpi.grip.core.metrics;

import edu.wpi.grip.core.events.BenchmarkEvent;
import edu.wpi.grip.core.events.RunStartedEvent;
import edu.wpi.grip.core.events.RunStoppedEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link BenchmarkRunner}.
 */
public class BenchmarkRunnerTest {

  private EventBus eventBus;
  private BenchmarkRunner benchmarkRunner;

  @Before
  public void setUp() {
    eventBus = new EventBus();
    benchmarkRunner = new BenchmarkRunner(eventBus);
    eventBus.register(benchmarkRunner);
  }

  @Test
  @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert") // checkDefaultState() has asserts
  public void testWhenNotBenchmarking() {
    checkDefaultState();
    eventBus.post(new RunStartedEvent());
    checkDefaultState();
    eventBus.post(new RunStoppedEvent());
    checkDefaultState();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeRuns() {
    benchmarkRunner.run(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroRuns() {
    benchmarkRunner.run(0);
  }

  @Test
  public void testCancelBenchmark() {
    benchmarkRunner.run(1);
    assertTrue("Runner is not running after cal to run()", benchmarkRunner.isRunning());
    assertEquals("Runs remaining != 1", 1, benchmarkRunner.getRunsRemaining());
    eventBus.post(BenchmarkEvent.finished());
    checkDefaultState();
  }

  @Test
  public void testRuns() {
    AtomicReference<BenchmarkEvent> benchmarkEventStart = new AtomicReference<>();
    AtomicReference<BenchmarkEvent> benchmarkEventFinish = new AtomicReference<>();
    eventBus.register(new Object() {
      @Subscribe
      public void onBenchmarkEvent(BenchmarkEvent event) {
        if (event.isStart()) {
          benchmarkEventStart.set(event);
        } else {
          benchmarkEventFinish.set(event);
        }
      }
    });
    checkDefaultState();
    benchmarkRunner.run(2);
    assertNotNull("No 'started' benchmark event fired", benchmarkEventStart.get());
    assertTrue("Runner is not running after call to run()", benchmarkRunner.isRunning());
    assertEquals("Runs remaining != 2", 2, benchmarkRunner.getRunsRemaining());
    eventBus.post(new RunStartedEvent());
    assertTrue("Runner is not running after first start", benchmarkRunner.isRunning());
    assertEquals("Runs remaining != 1", 1, benchmarkRunner.getRunsRemaining());
    eventBus.post(new RunStoppedEvent());
    eventBus.post(new RunStartedEvent());
    assertTrue("Runner is not running after second start", benchmarkRunner.isRunning());
    assertEquals("Runs remaining != 0", 0, benchmarkRunner.getRunsRemaining());
    eventBus.post(new RunStoppedEvent());
    checkDefaultState();
    assertNotNull("No 'finished' benchmark event fired", benchmarkEventFinish.get());
  }

  private void checkDefaultState() {
    assertFalse("Runner should not be running", benchmarkRunner.isRunning());
    assertEquals("Runs remaining != 0", 0, benchmarkRunner.getRunsRemaining());
  }

}
