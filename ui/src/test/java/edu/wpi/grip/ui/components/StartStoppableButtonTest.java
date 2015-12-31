package edu.wpi.grip.ui.components;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.StartStoppable;
import edu.wpi.grip.core.events.StartedStoppedEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

public class StartStoppableButtonTest extends ApplicationTest {

    private EventBus eventBus;
    private MockStartStoppable startStoppable;
    private StartStoppableButton startStoppableButton;

    class MockStartStoppable implements StartStoppable {

        private final EventBus eventBus;
        private boolean running;

        private MockStartStoppable(EventBus eventBus, boolean initialRunningState) {
            this.eventBus = eventBus;
            this.running = initialRunningState;
        }

        @Override
        public void start() throws IOException {
            this.running = true;
            this.eventBus.post(new StartedStoppedEvent(this));
        }

        @Override
        public void stop() throws TimeoutException, IOException {
            this.running = false;
            this.eventBus.post(new StartedStoppedEvent(this));
        }

        @Override
        public boolean isStarted() {
            return running;
        }
    }

    @Override
    public void start(Stage stage) {
        eventBus = new EventBus();
        startStoppable = new MockStartStoppable(eventBus, false);
        startStoppableButton = new StartStoppableButton(eventBus, startStoppable);
        eventBus.register(startStoppableButton);
        Scene scene = new Scene(startStoppableButton, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testInitialState() {
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(startStoppableButton, NodeMatchers.hasChild("." + StartStoppableButton.STOPPED_STYLE_CLASS));
    }

    @Test
    public void testStartedTogglesState() throws Exception {
        startStoppable.start();
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(startStoppableButton, NodeMatchers.hasChild("." + StartStoppableButton.STARTED_STYLE_CLASS));
        assertTrue("The button should be selected", startStoppableButton.isSelected());
    }

    @Test
    public void testStoppedTogglesState() throws Exception {
        startStoppable.stop();
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(startStoppableButton, NodeMatchers.hasChild("." + StartStoppableButton.STOPPED_STYLE_CLASS));
        assertFalse("The button should not be selected", startStoppableButton.isSelected());
    }

    @Test
    public void testClickToStart() throws Exception {
        clickOn(startStoppableButton);
        assertTrue("The start stoppable should have started", startStoppable.isStarted());
    }

    @Test
    public void testClickToStop() throws Exception {
        startStoppable.start();
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(startStoppableButton);
        assertFalse("The start stoppable should have been stopped", startStoppable.isStarted());
    }
}