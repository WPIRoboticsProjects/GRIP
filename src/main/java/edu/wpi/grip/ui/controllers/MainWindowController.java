package edu.wpi.grip.ui.controllers;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.SetSinkEvent;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;
import edu.wpi.grip.core.operations.PythonScriptOperation;
import edu.wpi.grip.core.sinks.DummySink;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.core.sources.WebcamSource;
import edu.wpi.grip.generated.CVOperations;
import edu.wpi.grip.ui.PaletteView;
import edu.wpi.grip.ui.pipeline.PipelineView;
import edu.wpi.grip.ui.preview.PreviewsView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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

    private final Operation webcam = new Operation() {
        private OutputSocket[] outputSockets;
        private WebcamSource webcamSource;

        @Override
        public String getName() {
            return "Webcam";
        }

        @Override
        public String getDescription() {
            return "Gets a Video Feed from a webcamera";
        }

        @Override
        public InputSocket<?>[] createInputSockets(EventBus eventBus) {
            return new InputSocket<?>[0];
        }

        @Override
        public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
            webcamSource = new WebcamSource(eventBus);
            webcamSource.startVideo(0);
            this.outputSockets = webcamSource.getOutputSockets();
            return this.outputSockets;
        }

        @Override
        public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        }

        @Subscribe
        public void onStepRemoved(StepRemovedEvent event){
            if (event.getStep().getOutputSockets().equals(this.outputSockets)){
                try {
                    webcamSource.stopVideo();
                } catch (TimeoutException e) {
                    throw new IllegalStateException("Could not stop video source", e);
                }
            }
        }
    };

    private final PythonScriptOperation gompeiOperation;
    private final PythonScriptOperation sampleFilter;

    public MainWindowController() throws IOException {
        this.gompeiOperation = new PythonScriptOperation(getClass().getResource("/edu/wpi/grip/scripts/gompei.py"));
        this.sampleFilter = new PythonScriptOperation(getClass().getResource("/edu/wpi/grip/scripts/sample-filter.py"));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PreviewsView previewPaneView = new PreviewsView(eventBus);

        PaletteView paletteView = new PaletteView(eventBus);

        // REGISTER THE WEBCAMERA TO TAKE EVENTS
        eventBus.register(this.webcam);

        List<Operation> operationList = new ArrayList(
                Arrays.asList(
                        this.webcam,
                        this.add,
                        this.multiply,
                        this.gompeiOperation,
                        this.sampleFilter
                )
        );
        operationList.addAll(CVOperations.OPERATIONS);

        paletteView.operationsProperty().addAll(
                operationList.stream()
                        .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                        .collect(Collectors.toList())
        );


        PipelineView pipelineView = new PipelineView(eventBus, new Pipeline(this.eventBus));

        this.topPane.getItems().addAll(previewPaneView, paletteView);
        this.bottomPane.setContent(pipelineView);


        final ImageFileSource source1 = new ImageFileSource(eventBus);
        source1.loadImage(getClass().getResource("/edu/wpi/grip/images/fall-gompei.jpeg"));

        final ImageFileSource source2 = new ImageFileSource(eventBus);
        source2.loadImage(getClass().getResource("/edu/wpi/grip/images/winter-gompei.jpeg"));

        this.eventBus.post(new SourceAddedEvent(source1));
        this.eventBus.post(new SourceAddedEvent(source2));
        this.eventBus.post(new SetSinkEvent(new DummySink()));
    }
}

