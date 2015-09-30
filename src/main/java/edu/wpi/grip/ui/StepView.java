package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.StepRemovedEvent;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that shows a step in the pipeline.  This control shows the name of the operation as well as a list
 * of input sockets and output sockets.
 */
public class StepView extends AnchorPane implements Initializable {
    @FXML
    private TitledPane stepPane;

    @FXML
    private ImageView icon;

    @FXML
    private Button deleteButton;

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
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stepPane.setText(this.step.getOperation().getName());
        this.stepPane.setAlignment(Pos.TOP_LEFT);
        this.stepPane.setContentDisplay(ContentDisplay.LEFT);

        this.step.getOperation().getIcon().ifPresent(icon -> {
            this.icon.setImage(new Image(icon));
            this.icon.setFitWidth(Screen.getPrimary().getDpi() * 0.25);
            this.icon.setFitHeight(Screen.getPrimary().getDpi() * 0.25);
        });

        // Add a SocketControlView for each input socket and output socket
        for (InputSocket<?> inputSocket : this.step.getInputSockets()) {
            this.inputs.getChildren().add(new InputSocketView(this.eventBus, inputSocket));
        }

        for (OutputSocket<?> outputSocket : this.step.getOutputSockets()) {
            this.outputs.getChildren().add(new OutputSocketView(this.eventBus, outputSocket));
        }

        // When the delete button is pressed, remove this step.
        this.deleteButton.setOnMouseClicked(mouseEvent -> this.eventBus.post(new StepRemovedEvent(this.step)));
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
}
