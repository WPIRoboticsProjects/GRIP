package edu.wpi.grip.ui;


import edu.wpi.grip.core.AdditionOperation;
import edu.wpi.grip.core.PipelineRunner;
import edu.wpi.grip.core.events.OperationAddedEvent;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static org.testfx.api.FxAssert.verifyThat;


public class MainWindowTest extends ApplicationTest {

    private PipelineRunner pipelineRunner;

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void start(Stage stage) throws Exception {
        final Main main = new Main();
        main.start(stage);

        final OperationListController operationList = main.injector.getInstance(OperationListController.class);
        pipelineRunner = main.injector.getInstance(PipelineRunner.class);
        operationList.clearOperations();
        operationList.onOperationAdded(new OperationAddedEvent(new AdditionOperation()));
    }

    @After
    public void tearDown() {
        pipelineRunner.stopAsync().awaitTerminated();
    }

    @Test
    @Ignore
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void testShouldCreateNewOperationInPipelineView() {
        // Given:
        clickOn("#add-operation");

        WaitForAsyncUtils.waitForFxEvents();

        // Then:
        verifyThat(".pipeline", NodeMatchers.hasChild(".add-step"));
    }


}