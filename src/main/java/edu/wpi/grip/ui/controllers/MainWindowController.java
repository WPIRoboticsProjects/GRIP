package edu.wpi.grip.ui.controllers;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.ErrorAddedEvent;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.events.SetSinkEvent;
import edu.wpi.grip.core.operations.PythonScriptOperation;
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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import org.controlsfx.control.NotificationPane;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The Controller for the application window.  Most of this class is throwaway code to demonstrate the current
 * features until a fully usable application is implemented.
 */
public class MainWindowController implements Initializable {
    private final EventBus eventBus = new EventBus((exception, context) -> {
        new IllegalStateException("Could not dispatch event: "
                + context.getSubscriber() + " to " + context.getSubscriberMethod(), exception).printStackTrace(System.err);
    });

    @FXML
    private NotificationPane notificationPane;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.topPane.getItems().addAll(new PreviewsView(this.eventBus), new PaletteView(this.eventBus));
        this.bottomPane.setContent(new PipelineView(this.eventBus, new Pipeline(this.eventBus)));

        this.notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);

        // Add the default built-in operations to the palette
        this.eventBus.post(new OperationAddedEvent(new BlurOperation()));
        this.eventBus.post(new OperationAddedEvent(new DesaturateOperation()));
        this.eventBus.post(new OperationAddedEvent(new RGBThresholdOperation()));
        this.eventBus.post(new OperationAddedEvent(new HSVThresholdOperation()));
        this.eventBus.post(new OperationAddedEvent(new HSLThresholdOperation()));
        this.eventBus.post(new OperationAddedEvent(new FindBlobsOperation()));
        this.eventBus.post(new OperationAddedEvent(new FindLinesOperation()));
        this.eventBus.post(new OperationAddedEvent(new FilterLinesOperation()));
        this.eventBus.post(new OperationAddedEvent(new MinMaxLoc()));
        this.eventBus.post(new OperationAddedEvent(new NewPointOperation()));
        this.eventBus.post(new OperationAddedEvent(new NewSizeOperation()));
        this.eventBus.post(new OperationAddedEvent(new MatFieldAccessor()));

        // TODO: Remove these before release
        this.eventBus.post(new OperationAddedEvent(this.add));
        this.eventBus.post(new OperationAddedEvent(this.multiply));
        scripts.stream().map(MainWindowController::loadOperation)
                .map(OperationAddedEvent::new).forEach(this.eventBus::post);

        // Add all of the auto-generated OpenCV operations
        CVOperations.addOperations(this.eventBus);


        this.eventBus.post(new SetSinkEvent(new DummySink()));
        this.eventBus.register(this);
    }

    @Subscribe
    public void onErrorStatusAdded(ErrorAddedEvent event) {
        Platform.runLater(() -> {
            event.getDetailedDescription().ifPresent(description -> this.notificationPane.setTooltip(new Tooltip(description)));
            this.notificationPane.show(event.getException().getMessage());
        });
    }
}

