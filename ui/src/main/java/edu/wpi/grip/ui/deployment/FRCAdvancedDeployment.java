package edu.wpi.grip.ui.deployment;

import edu.wpi.grip.ui.util.deployment.DeployedInstanceManager;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.io.OutputStream;
import java.net.InetAddress;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FRCAdvancedDeployment extends DeploymentOptionsController {


    private final DeployedInstanceManager.Factory deployedInstanceManagerFactor;
    private final Supplier<OutputStream> stdOut, stdErr;

    private TextField address;

    protected FRCAdvancedDeployment(DeployedInstanceManager.Factory deployedInstanceManagerFactor,
                                    Consumer<DeployedInstanceManager> onDeployCallback,
                                    Supplier<OutputStream> stdOut,
                                    Supplier<OutputStream> stdErr) {
        super("FRC Advanced", onDeployCallback);
        this.deployedInstanceManagerFactor = deployedInstanceManagerFactor;
        this.stdOut = stdOut;
        this.stdErr = stdErr;

    }

    @Override
    protected void postInit() {
        this.address = new TextField();
        this.address.setPromptText("roborio-[team number]-frc.local");

        this.address.textProperty().addListener((observable, oldValue, newValue) -> {
            // Enable the "Deploy" button only if the user has entered something.
            // Note: InetAddresses.isInetAddress only works for IP address not mdns names
            if (newValue.equals("")) {
                getDeployButton().setDisable(true);
            } else {
                getDeployButton().setDisable(false);
            }
        });
        getDeployButton().setDisable(true);

        final Label textFieldInput = new Label("Address/IP:");
        textFieldInput.setLabelFor(this.address);

        getOptionsGrid().addRow(0, textFieldInput, this.address);
    }

    @Override
    protected Promise<DeployedInstanceManager, String, String> onDeploy() {
        final Deferred<DeployedInstanceManager, String, String> deploy = new DeferredObject<>();
        checkInetAddressReachable(() -> InetAddress.getByName(address.getText()))
                .progress(deploy::notify)
                .fail(throwable -> deploy.reject(throwable.getMessage()))
                .done((address) -> deploy.resolve(deployedInstanceManagerFactor.createFRC(address, stdOut, stdErr)));
        return deploy.promise();
    }
}