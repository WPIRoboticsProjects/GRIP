package edu.wpi.grip.ui.controllers;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.StepAddedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;
import edu.wpi.grip.core.operations.PythonScriptOperation;
import edu.wpi.grip.ui.PaletteView;
import edu.wpi.grip.ui.PipelineView;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

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

    private PipelineView pipelineView;


    private final Operation add = new PythonScriptOperation(
            "import edu.wpi.grip.core as grip\n" +
            "import java.lang.Number\n" +

            "name = 'Python Add'\n" +
            "description = 'Compute the sum of two numbers using Python'\n" +

            "inputs = [\n" +
            "    grip.SocketHint('a', java.lang.Number, grip.SocketHint.View.SLIDER, [0, 1], 0.1),\n" +
            "    grip.SocketHint('b', java.lang.Number, grip.SocketHint.View.SLIDER, [0, 1], 0.4),\n" +
            "]\n" +

            "outputs = [\n" +
            "    grip.SocketHint('sum', java.lang.Number),\n" +
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
            "    grip.SocketHint('a', java.lang.Number, grip.SocketHint.View.SLIDER, [0, 1], 0.5),\n" +
            "    grip.SocketHint('b', java.lang.Number, grip.SocketHint.View.SLIDER, [0, 1], 0.5),\n" +
            "]\n" +

            "outputs = [\n" +
            "    grip.SocketHint('product', java.lang.Number),\n" +
            "]\n" +

            "def perform(a, b):\n" +
            "    return a * b\n"
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PaletteView paletteView = new PaletteView(eventBus);
        paletteView.operationsProperty().addAll(this.add, this.multiply);
        this.topPane.getItems().add(paletteView);

        PipelineView pipelineView = new PipelineView(eventBus, new Pipeline(this.eventBus));
        this.bottomPane.setContent(pipelineView);

        /* Add and remove some steps and connections */
        new Thread(() -> {
            try {
                Thread.sleep(1000);

                Step step1 = new Step(eventBus, add);
                eventBus.post(new StepAddedEvent(step1));

                Thread.sleep(1000);

                Step step2 = new Step(eventBus, multiply);
                eventBus.post(new StepAddedEvent(step2));

                Thread.sleep(1000);

                Step step3 = new Step(eventBus, add);
                eventBus.post(new StepAddedEvent(step3));

                Thread.sleep(1000);

                @SuppressWarnings("unchecked")
                Connection<Number> connection1 = new Connection<>(eventBus, (Socket<Number>) step1.getOutputSockets()[0],
                        (Socket<Number>) step2.getInputSockets()[0]);
                eventBus.post(new ConnectionAddedEvent(connection1));

                Thread.sleep(1000);

                @SuppressWarnings("unchecked")
                Connection<Number> connection2 = new Connection<>(eventBus, (Socket<Number>) step2.getOutputSockets()[0],
                        (Socket<Number>) step3.getInputSockets()[1]);
                eventBus.post(new ConnectionAddedEvent(connection2));

                Thread.sleep(1000);

                eventBus.post(new ConnectionRemovedEvent(connection1));

                Thread.sleep(1000);

                eventBus.post(new StepRemovedEvent(step3));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

