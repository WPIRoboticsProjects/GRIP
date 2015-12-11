package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.StepMovedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;
import edu.wpi.grip.ui.pipeline.input.InputSocketView;
import edu.wpi.grip.ui.pipeline.input.InputSocketViewFactory;
import edu.wpi.grip.ui.util.StyleClassNameUtility;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

/**
 * A JavaFX control that shows a step in the pipeline.  This control shows the name of the operation as well as a list
 * of input sockets and output sockets.
 */
public class StepController {

    @FXML private VBox root;
    @FXML private Labeled title;
    @FXML private ImageView icon;
    @FXML private VBox inputs;
    @FXML private VBox outputs;
    @Inject private EventBus eventBus;

    private final Step step;

    /**
     * Used for assisted injects.  Guice will automatically create an instance of this interface so we can create
     * step controllers.  This lets us use injection with StepController even though it requires a {@link Step}
     * (which is not an injected dependency).
     */
    public interface Factory {
        StepController create(Step step);
    }

    @Inject
    public StepController(@Assisted Step step) {
        this.step = step;
    }

    public void initialize() {
        root.getStyleClass().add(StyleClassNameUtility.classNameFor(step));
        title.setText(step.getOperation().getName());
        step.getOperation().getIcon().ifPresent(icon -> this.icon.setImage(new Image(icon)));

        // Add a SocketControlView for each input socket and output socket
        for (InputSocket<?> inputSocket : step.getInputSockets()) {
            inputs.getChildren().add(InputSocketViewFactory.createInputSocketView(eventBus, inputSocket));
        }

        for (OutputSocket<?> outputSocket : step.getOutputSockets()) {
            outputs.getChildren().add(new OutputSocketView(eventBus, outputSocket));
        }
    }

    /**
     * @return An unmodifiable list of {@link InputSocketView}s corresponding to the input sockets of this step
     */
    @SuppressWarnings("unchecked")
    public ObservableList<InputSocketView> getInputSockets() {
        return (ObservableList) inputs.getChildrenUnmodifiable();
    }

    /**
     * @return An unmodifiable list of {@link InputSocketView}s corresponding to the output sockets of this step
     */
    @SuppressWarnings("unchecked")
    public ObservableList<OutputSocketView> getOutputSockets() {
        return (ObservableList) outputs.getChildrenUnmodifiable();
    }

    public VBox getRoot() {
        return root;
    }

    public Step getStep() {
        return step;
    }

    @FXML
    private void deleteStep() {
        eventBus.post(new StepRemovedEvent(step));
    }

    @FXML
    private void moveStepLeft() {
        eventBus.post(new StepMovedEvent(step, -1));
    }

    @FXML
    private void moveStepRight() {
        eventBus.post(new StepMovedEvent(step, +1));
    }
}
