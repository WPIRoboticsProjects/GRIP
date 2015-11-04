package edu.wpi.grip.ui.controllers;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.EventHistory;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.RedoPublishedEvent;
import edu.wpi.grip.core.events.SetSinkEvent;
import edu.wpi.grip.core.events.UndoPublishedEvent;
import edu.wpi.grip.core.operations.PythonScriptOperation;
import edu.wpi.grip.core.operations.composite.BlurOperation;
import edu.wpi.grip.core.operations.composite.RGBThresholdOperation;
import edu.wpi.grip.core.operations.composite.DesaturateOperation;
import edu.wpi.grip.core.operations.opencv.MatFieldAccessor;
import edu.wpi.grip.core.operations.opencv.NewPointOperation;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;
import edu.wpi.grip.core.sinks.DummySink;
import edu.wpi.grip.generated.CVOperations;
import edu.wpi.grip.ui.PaletteView;
import edu.wpi.grip.ui.pipeline.PipelineView;
import edu.wpi.grip.ui.preview.PreviewsView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;

import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * The Controller for the application window.  Most of this class is throwaway code to demonstrate the current
 * features until a fully usable application is implemented.
 */
public class MainWindowController implements Initializable {
    private final EventBus eventBus = new EventBus((exception, context) -> {
        new IllegalStateException("Could not dispatch event: "
                + context.getSubscriber() + " to " + context.getSubscriberMethod(), exception).printStackTrace(System.err);
    });

    private final EventHistory eventHistory;

    @FXML
    private MenuItem undoMenuItem;

    @FXML
    private MenuItem redoMenuItem;

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
            "    grip.SocketHint('sum', java.lang.Number, 0.0, grip.SocketHint.View.NONE, None, True),\n" +
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
            "    grip.SocketHint('product', java.lang.Number, 0.0, grip.SocketHint.View.NONE, None, True),\n" +
            "]\n" +

            "def perform(a, b):\n" +
            "    return a * b\n"
    );

    private final static List<URL> scripts = Arrays.asList(
            MainWindowController.class.getResource("/edu/wpi/grip/scripts/sample-filter.py")
    );

    private static Operation loadOperation(URL url) {
        try {
            return new PythonScriptOperation(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MainWindowController(){
        this.eventHistory = new EventHistory(this.eventBus);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PreviewsView previewPaneView = new PreviewsView(eventBus);

        final PaletteView paletteView = new PaletteView(eventBus);

        paletteView.operationsProperty().addAll(
                new BlurOperation(),
                new RGBThresholdOperation(),
                new DesaturateOperation());

        paletteView.operationsProperty().addAll(scripts.stream()
                .map(MainWindowController::loadOperation)
                .collect(Collectors.toList()));

        paletteView.operationsProperty().addAll(this.add, this.multiply);

        List<Operation> allCVOperations = new ArrayList(CVOperations.OPERATIONS);
        allCVOperations.add(new NewPointOperation());
        allCVOperations.add(new NewSizeOperation());
        allCVOperations.add(new MatFieldAccessor());
        paletteView.operationsProperty().addAll(allCVOperations.stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .collect(Collectors.toList()));

        final PipelineView pipelineView = new PipelineView(eventBus, new Pipeline(this.eventBus));

        this.topPane.getItems().addAll(previewPaneView, paletteView);
        this.bottomPane.setContent(pipelineView);

        this.undoMenuItem.setOnAction((event)-> eventBus.post(new UndoPublishedEvent()));
        this.redoMenuItem.setOnAction((event)-> eventBus.post(new RedoPublishedEvent()));

        this.eventBus.post(new SetSinkEvent(new DummySink()));
    }
}

