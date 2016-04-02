package edu.wpi.grip.ui;


import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.ui.util.DPIUtility;
import edu.wpi.grip.ui.util.StyleClassNameUtility;
import edu.wpi.grip.util.GRIPCoreTestModule;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.Assert.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class MainWindowTest extends ApplicationTest {
    private static final String STEP_NOT_ADDED_MSG = "Step was not added to pipeline";
    private final GRIPCoreTestModule testModule = new GRIPCoreTestModule();
    private Pipeline pipeline;
    private PipelineRunner pipelineRunner;
    private Operation addOperation;
    private AdditionOperation additionOperation;

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void start(Stage stage) throws Exception {
        testModule.setUp();

        Injector injector = Guice.createInjector(Modules.override(testModule).with(new GRIPUIModule()));

        final Parent root =
                FXMLLoader.load(Main.class.getResource("MainWindow.fxml"), null, null, injector::getInstance);
        root.setStyle("-fx-font-size: " + DPIUtility.FONT_SIZE + "px");

        pipeline = injector.getInstance(Pipeline.class);
        pipelineRunner = injector.getInstance(PipelineRunner.class);
        final EventBus eventBus = injector.getInstance(EventBus.class);

        addOperation = new AddOperation();
        additionOperation = new AdditionOperation();

        eventBus.post(new OperationAddedEvent(addOperation));
        eventBus.post(new OperationAddedEvent(additionOperation));

        stage.setScene(new Scene(root));
        stage.show();
    }

    @After
    public void tearDown() {
        testModule.tearDown();
        pipelineRunner.stopAsync().awaitTerminated();
    }

    @Test
    public void testShouldCreateNewOperationInPipelineView() {
        // Given:
        clickOn(addOperation.getName());

        WaitForAsyncUtils.waitForFxEvents();

        // Then:
        final String cssSelector = "." + StyleClassNameUtility.classNameForStepHolding(addOperation);
        verifyThat(cssSelector, NodeMatchers.isNotNull());
        verifyThat(cssSelector, NodeMatchers.isVisible());

        assertEquals(STEP_NOT_ADDED_MSG, 1, pipeline.getSteps().size());
        assertEquals("Step added was not this addOperation", addOperation, pipeline.getSteps().get(0).getOperation());
    }

    @Test
    public void testDragOperationFromPaletteToPipeline() {
        // Given:
        drag(addOperation.getName())
                .dropTo(".steps");

        WaitForAsyncUtils.waitForFxEvents();

        // Then:
        final String cssSelector = "." + StyleClassNameUtility.classNameForStepHolding(addOperation);
        verifyThat(cssSelector, NodeMatchers.isNotNull());
        verifyThat(cssSelector, NodeMatchers.isVisible());

        assertEquals(STEP_NOT_ADDED_MSG, 1, pipeline.getSteps().size());
        assertEquals("Step added was not this addOperation", addOperation, pipeline.getSteps().get(0).getOperation());
    }

    @Test
    public void testDragOperationFromPaletteToLeftOfExistingStep() {
        // Run the same test as before
        testDragOperationFromPaletteToPipeline();

        // Now add a second step before it
        drag(additionOperation.getName())
                // We drag to the input socket hint handle because this will always be on the left side of the
                // step. This should cause the UI to put the new step on the left side
                .dropTo(StyleClassNameUtility.cssSelectorForInputSocketHandleOnStepHolding(addOperation));

        WaitForAsyncUtils.waitForFxEvents();

        // Then:
        final String cssSelector = "." + StyleClassNameUtility.classNameForStepHolding(additionOperation);
        verifyThat(cssSelector, NodeMatchers.isNotNull());
        verifyThat(cssSelector, NodeMatchers.isVisible());

        assertEquals(STEP_NOT_ADDED_MSG, 2, pipeline.getSteps().size());
        assertEquals("Step added was not added in the right place in the pipeline",
                additionOperation, pipeline.getSteps().get(0).getOperation());
    }

    @Test
    public void testDragOperationFromPaletteToRightOfExistingStep() {
        // Run the same test as before
        testDragOperationFromPaletteToPipeline();

        // Now add a second step after it
        drag(additionOperation.getName())
                // We drag to the output socket hint handle because this will always be on the right side of the
                // step. This should cause the UI to put the new step on the right side
                .dropTo(StyleClassNameUtility.cssSelectorForOutputSocketHandleOnStepHolding(addOperation));

        WaitForAsyncUtils.waitForFxEvents();

        // Then:
        final String cssSelector = "." + StyleClassNameUtility.classNameForStepHolding(additionOperation);
        verifyThat(cssSelector, NodeMatchers.isNotNull());
        verifyThat(cssSelector, NodeMatchers.isVisible());

        assertEquals(STEP_NOT_ADDED_MSG, 2, pipeline.getSteps().size());
        assertEquals("Step added was not added in the right place in the pipeline",
                additionOperation, pipeline.getSteps().get(1).getOperation());
    }


}