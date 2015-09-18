package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.Step;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

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

        // Add a SocketControlView for each input socket and output socket
        for (Socket<?> inputSocket : this.step.getInputSockets()) {
            this.inputs.getChildren().add(new SocketControlView(this.eventBus, inputSocket));
        }

        for (Socket<?> outputSocket: this.step.getOutputSockets()) {
            this.outputs.getChildren().add(new SocketControlView(this.eventBus, outputSocket));
        }
    }
}
