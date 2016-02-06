package edu.wpi.grip.ui.components;

import com.google.common.util.concurrent.AbstractIdleService;
import edu.wpi.grip.core.util.service.AutoRestartingService;
import edu.wpi.grip.core.util.service.RestartableService;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

public class StartStoppableButtonTest extends ApplicationTest {

    private RestartableService restartableService;
    private StartStoppableButton startStoppableButton;


    @Override
    public void start(Stage stage) {
        restartableService = new AutoRestartingService<>(() -> new AbstractIdleService() {
            @Override
            protected void startUp() {
            }

            @Override
            protected void shutDown() {
            }
        }, () -> false);
        startStoppableButton = new StartStoppableButton(restartableService);
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
        restartableService.startAsync().awaitRunning();
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(startStoppableButton, NodeMatchers.hasChild("." + StartStoppableButton.STARTED_STYLE_CLASS));
        assertTrue("The button should be selected", startStoppableButton.isSelected());
    }

    @Test
    public void testStoppedTogglesState() throws Exception {
        restartableService.stopAsync().awaitTerminated();
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(startStoppableButton, NodeMatchers.hasChild("." + StartStoppableButton.STOPPED_STYLE_CLASS));
        assertFalse("The button should not be selected", startStoppableButton.isSelected());
    }

    @Test
    public void testClickToStart() throws Exception {
        clickOn(startStoppableButton);
        restartableService.awaitRunning(1, TimeUnit.SECONDS);
        assertTrue("The start stoppable should have started", restartableService.isRunning());
    }

    @Test
    public void testClickToStop() throws Exception {
        restartableService.startAsync();
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(startStoppableButton);
        assertFalse("The start stoppable should have been stopped", restartableService.isRunning());
    }
}