package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Pipeline;
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

    public MainWindowView(final EventBus eventBus, final Pipeline pipeline) {
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
        final PaletteView paletteView = new PaletteView(eventBus);
        final PipelineView pipelineView = new PipelineView(eventBus, pipeline);

        this.topPane.getItems().addAll(previewsView, paletteView);
        this.bottomPane.setContent(pipelineView);
        pipelineView.prefHeightProperty().bind(this.bottomPane.heightProperty());
    }
}

