package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control fro the pipeline.  This control renders a list of steps.
 */
public class PipelineView extends HBox implements Initializable {
    @FXML
    private HBox steps;

    private final EventBus eventBus;
    private final Pipeline pipeline;

    public PipelineView(EventBus eventBus, Pipeline pipeline) {
        checkNotNull(eventBus);
        checkNotNull(pipeline);

        this.eventBus = eventBus;
        this.pipeline = pipeline;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Pipeline.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.eventBus.register(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (Step step : pipeline.getSteps()) {
            steps.getChildren().add(new StepView(this.eventBus, step));
        }
    }
}

