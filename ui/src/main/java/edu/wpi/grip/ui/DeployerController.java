package edu.wpi.grip.ui;


import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.components.StartStoppableButton;
import edu.wpi.grip.ui.deployment.DeploymentOptionsController;
import edu.wpi.grip.ui.deployment.DeploymentOptionsControllersFactory;
import edu.wpi.grip.ui.util.deployment.DeployedInstanceManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Singleton
@ParametrizedController(url = "DeployerPane.fxml")
public class DeployerController {

    @FXML
    private DialogPane root;

    @FXML
    private HBox controlsBox;

    @FXML
    private Accordion deploymentMethods;

    @FXML
    private TextArea stdOutStreamTextArea;

    @FXML
    private TextArea stdErrStreamTextArea;

    @FXML
    private ProgressBar progressIndicator;

    private final EventBus eventBus;
    private final StartStoppableButton.Factory startStopButtonFactory;
    private final DeploymentOptionsControllersFactory optionsControllersFactory;

    private class StreamToTextArea extends OutputStream {
        private final TextArea outputArea;

        public StreamToTextArea(TextArea outputArea) {
            this.outputArea = outputArea;
        }

        public StreamToTextArea reset() {
            outputArea.clear();
            return this;
        }

        @Override
        public void write(int i) throws IOException {
            outputArea.appendText(String.valueOf((char) i));
        }
    }


    public interface Factory {
        DeployerController create();
    }

    @Inject
    DeployerController(EventBus eventBus, StartStoppableButton.Factory startStopButtonFactory, DeploymentOptionsControllersFactory optionsControllersFactory) {
        this.eventBus = eventBus;
        this.startStopButtonFactory = startStopButtonFactory;
        this.optionsControllersFactory = optionsControllersFactory;
    }

    @FXML
    private void initialize() {
        final Supplier<OutputStream> out = () ->
                new PrintStream(new StreamToTextArea(stdOutStreamTextArea).reset(), false);
        final Supplier<OutputStream> err = () ->
                new PrintStream(new StreamToTextArea(stdErrStreamTextArea).reset(), false);
        deploymentMethods.getPanes().addAll(
                optionsControllersFactory
                        .createControllers(this::onDeploy, out, err)
                        .stream()
                        .map(DeploymentOptionsController::getRoot)
                        .collect(Collectors.toList()));
    }

    private void onDeploy(DeployedInstanceManager manager) {
        Platform.runLater(() -> {
            progressIndicator.setProgress(0);
            deploymentMethods.setDisable(true);
        });
        manager.deploy()
                .fail(throwable -> {
                    Platform.runLater(() -> {
                        stdErrStreamTextArea.setText("Failed to deploy\n" +
                                Throwables.getStackTraceAsString(throwable)
                        );
                        deploymentMethods.setDisable(false);
                    });
                })
                .progress(percent -> {
                    Platform.runLater(() -> progressIndicator.setProgress(percent));
                })
                .done(deployedManager -> {
                    Platform.runLater(() -> {
                        controlsBox.getChildren().add(startStopButtonFactory.create(deployedManager));
                        deploymentMethods.setDisable(true);
                        progressIndicator.setProgress(-1);
                    });
                });

    }

    public DialogPane getRoot() {
        return root;
    }
}

