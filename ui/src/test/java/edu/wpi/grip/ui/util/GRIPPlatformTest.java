package edu.wpi.grip.ui.util;

import com.google.common.eventbus.EventBus;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.jodah.concurrentunit.Waiter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.testfx.framework.junit.ApplicationTest;

import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GRIPPlatformTest extends ApplicationTest {

    private EventBus eventBus;
    private GRIPPlatform platform;
    private GRIPPlatform unRegisteredPlatform;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested

    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(new Pane(), 800, 600));
        stage.show();
    }


    @Before
    public void setUp() {
        this.eventBus = new EventBus();
        this.platform = new GRIPPlatform(eventBus, Logger.getLogger(GRIPPlatform.class.getName()));
        eventBus.register(this.platform);

        // This one should not be registered on the event bus!
        this.unRegisteredPlatform = new GRIPPlatform(eventBus, Logger.getLogger(GRIPPlatform.class.getName()));
    }

    @Test
    public void testRunAsSoonAsPossibleRunsInCorrectThreadWhenNotCalledFromFXThread() throws Exception {
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
            assertTrue("When running in the JavaFX thread this should have run immediately in the same thread",
                    hasRun[0]);
        });
    }

    @Test
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
        final boolean hasRun [] = {false};

        Thread.currentThread().interrupt();
        platform.runAsSoonAsPossible(() -> {
            hasRun[0] = true;
        });
        assertFalse("runAsSoonAsPossible ran when interrupted", hasRun[0]);
    }

}