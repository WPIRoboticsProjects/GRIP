package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.StepAddedEvent;
import edu.wpi.grip.ui.util.StyleClassNameUtility;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.fail;
import static org.testfx.api.FxAssert.verifyThat;

public class PipelineViewTest extends ApplicationTest {
    private EventBus eventBus;
    private Pipeline pipeline;
    private final AdditionOperation additionOperation = new AdditionOperation();
    private final SubtractionOperation subtractionOperation = new SubtractionOperation();

    @Override
    public void start(Stage stage) throws Exception {
        this.eventBus = new EventBus();
        this.pipeline = new Pipeline(eventBus);
        final Scene scene = new Scene(new PipelineView(eventBus, pipeline), 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testBasicAddOperationToPipeline() {
        addAdditionOperation();
    }

    @Test
    public void testConnectingTwoOperations() {
        // Given
        Step addStep = addOperation(1, additionOperation);
        Step subtractStep = addOperation(1, subtractionOperation);

        // When
        drag(".pipeline ." + StyleClassNameUtility.classNameFor(addStep) + " .socket-handle.output", MouseButton.PRIMARY)
                .dropTo(".pipeline ." + StyleClassNameUtility.classNameFor(subtractStep) + " .socket-handle.input");

        // Then
        Connection connection = assertStepConnected("The add step did not connect to the subtract step", addStep, subtractStep);
        verifyThat(".pipeline", NodeMatchers.hasChildren(1, "." + StyleClassNameUtility.classNameFor(connection)));

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

    private Step addOperation(int count, Operation operation) {
        final Step step = new Step(eventBus, operation);
        eventBus.post(new StepAddedEvent(step));

        // Wait for the event to propagate to the UI
        sleep(1, TimeUnit.SECONDS);

        verifyThat(".pipeline", NodeMatchers.hasChildren(count, "." + StyleClassNameUtility.classNameFor(step)));
        return step;
    }


}