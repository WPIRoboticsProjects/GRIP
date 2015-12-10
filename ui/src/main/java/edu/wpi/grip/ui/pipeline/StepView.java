package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that shows a step in the pipeline.  This control shows the name of the operation as well as a list
 * of input sockets and output sockets.
 */
public class StepView extends VBox {

    @FXML
    private Labeled title;

    @FXML
    private ImageView icon;

    @FXML
    private VBox inputs;

    @FXML
    private VBox outputs;

    private final EventBus eventBus;
    private final Step step;

    public StepView(EventBus eventBus, Step step) {
        checkNotNull(eventBus);
        checkNotNull(step);

        this.eventBus = eventBus;
        this.step = step;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Step.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.getStyleClass().add(StyleClassNameUtility.classNameFor(this.step));
        this.title.setText(this.step.getOperation().getName());
        this.step.getOperation().getIcon().ifPresent(icon -> this.icon.setImage(new Image(icon)));

        // Add a SocketControlView for each input socket and output socket
        for (InputSocket<?> inputSocket : this.step.getInputSockets()) {
            this.inputs.getChildren().add(InputSocketViewFactory.createInputSocketView(eventBus, inputSocket));
        }

        for (OutputSocket<?> outputSocket : this.step.getOutputSockets()) {
            this.outputs.getChildren().add(new OutputSocketView(this.eventBus, outputSocket));
        }
    }

    /**
     * @return An unmodifiable list of {@link InputSocketView}s corresponding to the input sockets of this step
     */
    @SuppressWarnings("unchecked")
    public ObservableList<InputSocketView> getInputSockets() {
        return (ObservableList) this.inputs.getChildrenUnmodifiable();
    }

    /**
     * @return An unmodifiable list of {@link InputSocketView}s corresponding to the output sockets of this step
     */
    @SuppressWarnings("unchecked")
    public ObservableList<OutputSocketView> getOutputSockets() {
        return (ObservableList) this.outputs.getChildrenUnmodifiable();
    }

    public Step getStep() {
        return this.step;
    }

    @FXML
    private void deleteStep() {
        this.eventBus.post(new StepRemovedEvent(this.step));
    }

    @FXML
    private void moveStepLeft() {
        this.eventBus.post(new StepMovedEvent(this.step, -1));
    }

    @FXML
    private void moveStepRight() {
        this.eventBus.post(new StepMovedEvent(this.step, +1));
    }
}
