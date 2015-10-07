package edu.wpi.grip.ui.controllers;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.SetSinkEvent;
import edu.wpi.grip.core.operations.PythonScriptOperation;
import edu.wpi.grip.core.sinks.DummySink;
import edu.wpi.grip.ui.PaletteView;
import edu.wpi.grip.ui.pipeline.PipelineView;
import edu.wpi.grip.ui.preview.PreviewsView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The Controller for the application window.  Most of this class is throwaway code to demonstrate the current
 * features until a fully usable application is implemented.
 */
public class MainWindowController implements Initializable {
    private final EventBus eventBus = new EventBus();

    @FXML
    private SplitPane topPane;

    @FXML
    private ScrollPane bottomPane;

    private final Operation add = new PythonScriptOperation(
            "import edu.wpi.grip.core as grip\n" +
            "import java.lang.Number\n" +

            "name = 'Python Add'\n" +
            "description = 'Compute the sum of two numbers using Python'\n" +

            "inputs = [\n" +
            "    grip.SocketHint('a', java.lang.Number, 0.1, grip.SocketHint.View.SLIDER, [0, 1]),\n" +
            "    grip.SocketHint('b', java.lang.Number, 0.4, grip.SocketHint.View.SLIDER, [0, 1]),\n" +
            "]\n" +

            "outputs = [\n" +
            "    grip.SocketHint('sum', java.lang.Number, 0.0),\n" +
            "]\n" +

            "def perform(a, b):\n" +
            "    return a + b\n"
    );

    private final Operation multiply = new PythonScriptOperation(
            "import edu.wpi.grip.core as grip\n" +
            "import java.lang.Number\n" +

            "name = 'Python Multiply'\n" +
            "description = 'Compute the product of two numbers using Python'\n" +

            "inputs = [\n" +
            "    grip.SocketHint('a', java.lang.Number, 0.5, grip.SocketHint.View.SLIDER, [0, 1]),\n" +
            "    grip.SocketHint('b', java.lang.Number, 0.5, grip.SocketHint.View.SLIDER, [0, 1]),\n" +
            "]\n" +

            "outputs = [\n" +
            "    grip.SocketHint('product', java.lang.Number, 0.0),\n" +
            "]\n" +

            "def perform(a, b):\n" +
            "    return a * b\n"
    );

    private final PythonScriptOperation gompeiOperation;

    public MainWindowController() throws IOException {
        this.gompeiOperation = new PythonScriptOperation(getClass().getResource("/edu/wpi/grip/scripts/gompei.py"));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PreviewsView previewPaneView = new PreviewsView(eventBus);

        PaletteView paletteView = new PaletteView(eventBus);
        paletteView.operationsProperty().addAll(this.add, this.multiply, this.gompeiOperation);

        PipelineView pipelineView = new PipelineView(eventBus, new Pipeline(this.eventBus));

        this.topPane.getItems().addAll(previewPaneView, paletteView);
        this.bottomPane.setContent(pipelineView);

        this.eventBus.post(new SetSinkEvent(new DummySink()));
    }
}

