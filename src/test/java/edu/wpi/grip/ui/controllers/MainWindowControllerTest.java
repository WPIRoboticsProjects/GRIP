package edu.wpi.grip.ui.controllers;


import edu.wpi.grip.Main;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import static org.testfx.api.FxAssert.verifyThat;


public class MainWindowControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Test
    public void testShouldCreateNewOperationInPipelineView() {
        // Given:
        clickOn("#python-multiply-operation");

        // Then:
        verifyThat(".pipeline", NodeMatchers.hasChildren(1, ".python-multiply-step"));
    }


}