package edu.wpi.grip.ui.pipeline;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.ui.GRIPUIModule;
import edu.wpi.grip.ui.util.StyleClassNameUtility;
import edu.wpi.grip.ui.util.TestAnnotationFXMLLoader;
import edu.wpi.grip.util.GRIPCoreTestModule;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.fail;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.api.FxAssert.verifyThatIter;

public class PipelineUITest extends ApplicationTest {

    private GRIPCoreTestModule testModule;
    private EventBus eventBus;
    private OperationMetaData additionOperation;
    private OperationMetaData subtractionOperation;
    private PipelineController pipelineController;
    private Pipeline pipeline;

    @Override
    public void start(Stage stage) {
        testModule = new GRIPCoreTestModule();
        testModule.setUp();
        final Injector injector = Guice.createInjector(Modules.override(testModule).with(new GRIPUIModule()));
        eventBus = injector.getInstance(EventBus.class);
        pipeline = injector.getInstance(Pipeline.class);
        InputSocket.Factory isf = injector.getInstance(InputSocket.Factory.class);
        OutputSocket.Factory osf = injector.getInstance(OutputSocket.Factory.class);
        additionOperation = new OperationMetaData(AdditionOperation.DESCRIPTION, () ->new AdditionOperation(isf, osf));
        subtractionOperation = new OperationMetaData(SubtractionOperation.DESCRIPTION, () -> new SubtractionOperation(isf, osf));
        pipelineController = injector.getInstance(PipelineController.class);
        final Scene scene = new Scene(TestAnnotationFXMLLoader.load(pipelineController), 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @After
    public void tearDown() {
        testModule.tearDown();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void testBasicAddOperationToPipeline() {
        addAdditionOperation();
    }

    @Test
    public void testConnectingTwoOperations() {
        // Given
        Step addStep = addOperation(1, additionOperation);
        Step subtractStep = addOperation(1, subtractionOperation);

        // When
        drag(StyleClassNameUtility.cssSelectorForOutputSocketHandleOn(addStep), MouseButton.PRIMARY)
                .dropTo(StyleClassNameUtility.cssSelectorForInputSocketHandleOn(subtractStep));

        // Then
        Connection connection = assertStepConnected("The add step did not connect to the subtract step", addStep, subtractStep);
        verifyThat(".pipeline", NodeMatchers.hasChildren(1, "." + StyleClassNameUtility.classNameFor(connection)));

    }

    @Test
    public void testMoveOperation() {
        final Step step1 = MockStep.createMockStepWithOperation();
        final Step step2 = MockStep.createMockStepWithOperation();
        final Step step3 = MockStep.createMockStepWithOperation();

        pipeline.addStep(step1);
        pipeline.addStep(step2);
        pipeline.addStep(step3);
        pipeline.moveStep(step2, +1);

        //sleep(1, TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        verifyThatIter(".step", new BaseMatcher<Iterable<Node>>() {
            @Override
            public boolean matches(Object item) {
                @SuppressWarnings("unchecked")
                final List<Node> stepsNodes = Lists.newArrayList((Iterable<Node>) item);

                assertEquals("Moving a step resulting in the number of steps changing", 3, stepsNodes.size());

                return pipelineController.findStepController(step1).getRoot().equals(stepsNodes.get(0))
                        && pipelineController.findStepController(step3).getRoot().equals(stepsNodes.get(1))
                        && pipelineController.findStepController(step2).getRoot().equals(stepsNodes.get(2));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("The steps were not in the expected order");
            }
        });
    }

    private Connection assertStepConnected(String message, Step output, Step input) {
        for (OutputSocket inputSocket : output.getOutputSockets()) {
            for (InputSocket outputSocket : input.getInputSockets()) {
                Optional<Connection> connection = inputSocket
                        .getConnections()
                        .stream()
                        .filter(c -> outputSocket.getConnections().contains(c)).findFirst();
                if (connection.isPresent()) {
                    return connection.get();
                }
            }
        }
        fail(message);
        return null;
    }

    private void addAdditionOperation() {
        addOperation(1, additionOperation);
    }

    private Step addOperation(int count, OperationMetaData operationMetaData) {
        final Step step = new Step.Factory(origin -> new MockExceptionWitness(eventBus, origin)).create(operationMetaData);
        pipeline.addStep(step);

        // Wait for the event to propagate to the UI
        sleep(1, TimeUnit.SECONDS);

        verifyThat(".pipeline", NodeMatchers.hasChildren(count, "." + StyleClassNameUtility.classNameFor(step)));
        return step;
    }


}
