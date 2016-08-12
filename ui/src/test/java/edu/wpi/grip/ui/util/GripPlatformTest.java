package edu.wpi.grip.ui.util;

import com.google.common.eventbus.EventBus;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import net.jodah.concurrentunit.Waiter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.testfx.framework.junit.ApplicationTest;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GripPlatformTest extends ApplicationTest {

  @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
      justification = "A JUnit rule -- used by JUnit")
  @Rule
  public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested
  private GripPlatform platform;
  private GripPlatform unRegisteredPlatform;

  @Override
  public void start(Stage stage) {
    stage.setScene(new Scene(new Pane(), 800, 600));
    stage.show();
  }


  @Before
  public void setUp() {
    EventBus eventBus = new EventBus();

    this.platform = new GripPlatform(eventBus);
    eventBus.register(this.platform);

    // This one should not be registered on the event bus!
    this.unRegisteredPlatform = new GripPlatform(eventBus);
  }

  @Test
  @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
  public void testRunAsSoonAsPossibleRunsInCorrectThreadWhenNotCalledFromFXThread() throws
      Exception {
    final Waiter waiter = new Waiter();
    platform.runAsSoonAsPossible(() -> {
      waiter.assertTrue(Platform.isFxApplicationThread());
      waiter.resume();
    });
    waiter.await();
  }

  @Test
  public void testRunAsSoonAsPossibleWhenCalledFromFXThread() {
    interact(() -> { // This will be running in the JavaFX thread
      final boolean[] hasRun = {false};
      unRegisteredPlatform.runAsSoonAsPossible(() -> {
        assertTrue("Should have run in the JavaFX thread!", Platform.isFxApplicationThread());
        hasRun[0] = true;
      });
      assertTrue("When running in the JavaFX thread this should have run immediately in the same "
              + "thread",
          hasRun[0]);
    });
  }

  @Test
  @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
  public void testRunAsSoonAsPossibleDoesNotDeadlockWhenRunInsideItself() throws Exception {
    final Waiter waiter = new Waiter();
    platform.runAsSoonAsPossible(() -> {
      waiter.assertTrue(Platform.isFxApplicationThread());
      platform.runAsSoonAsPossible(() -> waiter.assertTrue(Platform.isFxApplicationThread()));
      waiter.resume();
    });
    waiter.await();
  }

  @Test
  public void testRunAsSoonAsPossibleWillNotCallIfInterrupted() throws Exception {
    final boolean[] hasRun = {false};

    Thread.currentThread().interrupt();
    platform.runAsSoonAsPossible(() -> {
      hasRun[0] = true;
    });
    assertFalse("runAsSoonAsPossible ran when interrupted", hasRun[0]);
  }

}
