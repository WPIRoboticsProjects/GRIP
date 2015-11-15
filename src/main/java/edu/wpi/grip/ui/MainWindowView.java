package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Palette;
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
import edu.wpi.grip.ui.pipeline.PipelineView;
import edu.wpi.grip.ui.preview.PreviewsView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * The Controller for the application window.
 */
public class MainWindowView extends VBox {

    @FXML
    private SplitPane topPane;

    @FXML
    private ScrollPane bottomPane;

    public MainWindowView(final EventBus eventBus) {
        eventBus.register(this);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("FXML Failed to load", e);
        }

        final PreviewsView previewsView = new PreviewsView(eventBus);
        final PaletteView paletteView = new PaletteView(eventBus, new Palette(eventBus));
        final PipelineView pipelineView = new PipelineView(eventBus, new Pipeline(eventBus));

        this.topPane.getItems().addAll(previewsView, paletteView);
        this.bottomPane.setContent(pipelineView);
        pipelineView.prefHeightProperty().bind(this.bottomPane.heightProperty());

        // Add the default built-in operations to the palette
        eventBus.post(new OperationAddedEvent(new BlurOperation()));
        eventBus.post(new OperationAddedEvent(new DesaturateOperation()));
        eventBus.post(new OperationAddedEvent(new RGBThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new HSVThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new HSLThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new FindBlobsOperation()));
        eventBus.post(new OperationAddedEvent(new FindLinesOperation()));
        eventBus.post(new OperationAddedEvent(new FilterLinesOperation()));
        eventBus.post(new OperationAddedEvent(new MaskOperation()));
        eventBus.post(new OperationAddedEvent(new MinMaxLoc()));
        eventBus.post(new OperationAddedEvent(new NewPointOperation()));
        eventBus.post(new OperationAddedEvent(new NewSizeOperation()));
        eventBus.post(new OperationAddedEvent(new MatFieldAccessor()));

        // Add all of the auto-generated OpenCV operations
        CVOperations.addOperations(eventBus);

        eventBus.post(new SetSinkEvent(new DummySink()));
    }
}

