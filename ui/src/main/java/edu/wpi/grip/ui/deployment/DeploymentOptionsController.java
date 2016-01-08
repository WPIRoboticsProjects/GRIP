package edu.wpi.grip.ui.deployment;

import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.util.SupplierWithIO;
import edu.wpi.grip.ui.util.deployment.DeployedInstanceManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import org.jdeferred.DeferredCallable;
import org.jdeferred.DeferredManager;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;

import java.io.IOException;
import java.net.InetAddress;
import java.util.function.Consumer;

/**
 * A simple UI component that can be used to display options for deploying to a remote device using
 * asynchronous callbacks.
 */
@ParametrizedController(url = "DeploymentOptions.fxml")
public abstract class DeploymentOptionsController {

    @FXML
    private TitledPane root;

    @FXML
    private GridPane optionsGrid;

    @FXML
    private Label deployErrorText;

    @FXML
    private ProgressIndicator deploySpinner;

    @FXML
    private Button deployButton;

    private final String title;
    private final Consumer<DeployedInstanceManager> onDeployCallback;

    DeploymentOptionsController(String title, Consumer<DeployedInstanceManager> onDeployCallback) {
        this.title = title;
        this.onDeployCallback = onDeployCallback;
    }

    @FXML
    protected final void initialize() {
        root.setText(title);
        postInit();
    }

    /**
     * Called after the initialize method
     */
    protected abstract void postInit();

    protected abstract Promise<DeployedInstanceManager, String, String> onDeploy();

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void deploy() {
        deploySpinner.setVisible(true);
        deployButton.setDisable(true);
        onDeploy()
                .progress(this::setErrorText)
                .fail((text) -> {
                    setErrorText(text);
                    Platform.runLater(() -> {
                        deploySpinner.setVisible(false);
                        deployButton.setDisable(false);
                    });
                })
                .done((t) -> {
                    setErrorText("Success! Waiting for start.");
                    onDeployCallback.accept(t);
                    Platform.runLater(() -> {
                        deploySpinner.setVisible(false);
                        deployButton.setDisable(false);
                    });
                });
    }

    private void setErrorText(String text) {
        Platform.runLater(() -> {
            deployErrorText.setText(text);
            root.requestLayout();
            root.getScene().getWindow().sizeToScene();
        });
    }

    protected GridPane getOptionsGrid() {
        return optionsGrid;
    }

    protected Button getDeployButton() {
        return deployButton;
    }

    public TitledPane getRoot() {
        return root;
    }

    /**
     * Checks that an InetAddress is reachable asynchronously
     *
     * @param address The address supplier to check
     * @return A promise that is resolved when the InetAddress is determined to be reachable.
     */
    protected static Promise<InetAddress, Throwable, String> checkInetAddressReachable(SupplierWithIO<InetAddress> address) {
        final DeferredManager checkAddressDeferred = new DefaultDeferredManager();
        return checkAddressDeferred.when(new DeferredCallable<InetAddress, String>() {
            @Override
            public InetAddress call() throws IOException {
                final InetAddress inetAddress = address.getWithIO();
                final int attemptCount = 5;
                for (int i = 0; i < attemptCount; i++) {
                    if (inetAddress.isReachable(1000)) {
                        return inetAddress;
                    } else {
                        // i + 1 so that the attempt counter is zero based.
                        notify("Attempt " + i + 1 + "/" + attemptCount + " failed");
                    }
                }
                throw new IOException("Failed to connect");
            }
        });

    }
}
