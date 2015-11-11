package edu.wpi.grip.ui.controllers;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.events.SetSinkEvent;
import edu.wpi.grip.core.operations.composite.*;
import edu.wpi.grip.core.operations.opencv.MatFieldAccessor;
import edu.wpi.grip.core.operations.opencv.MinMaxLoc;
import edu.wpi.grip.core.operations.opencv.NewPointOperation;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;
import edu.wpi.grip.core.sinks.DummySink;
import edu.wpi.grip.generated.CVOperations;
import edu.wpi.grip.ui.PaletteView;
import edu.wpi.grip.ui.pipeline.PipelineView;
import edu.wpi.grip.ui.preview.PreviewsView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * The Controller for the application window.
 */
public class MainWindowController implements Initializable {
    private final EventBus eventBus = new EventBus((exception, context) -> {
        new IllegalStateException("Could not dispatch event: "
                + context.getSubscriber() + " to " + context.getSubscriberMethod(), exception).printStackTrace(System.err);
    });

    @FXML
    private SplitPane topPane;

    @FXML
    private ScrollPane bottomPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.topPane.getItems().addAll(new PreviewsView(this.eventBus), new PaletteView(this.eventBus));
        this.bottomPane.setContent(new PipelineView(this.eventBus, new Pipeline(this.eventBus)));

        // Add the default built-in operations to the palette
        this.eventBus.post(new OperationAddedEvent(new BlurOperation()));
        this.eventBus.post(new OperationAddedEvent(new DesaturateOperation()));
        this.eventBus.post(new OperationAddedEvent(new RGBThresholdOperation()));
        this.eventBus.post(new OperationAddedEvent(new HSVThresholdOperation()));
        this.eventBus.post(new OperationAddedEvent(new HSLThresholdOperation()));
        this.eventBus.post(new OperationAddedEvent(new FindBlobsOperation()));
        this.eventBus.post(new OperationAddedEvent(new FindLinesOperation()));
        this.eventBus.post(new OperationAddedEvent(new FilterLinesOperation()));
        this.eventBus.post(new OperationAddedEvent(new MaskOperation()));
        this.eventBus.post(new OperationAddedEvent(new MinMaxLoc()));
        this.eventBus.post(new OperationAddedEvent(new NewPointOperation()));
        this.eventBus.post(new OperationAddedEvent(new NewSizeOperation()));
        this.eventBus.post(new OperationAddedEvent(new MatFieldAccessor()));

        // Add all of the auto-generated OpenCV operations
        CVOperations.addOperations(this.eventBus);

        this.eventBus.post(new SetSinkEvent(new DummySink()));
    }
}

