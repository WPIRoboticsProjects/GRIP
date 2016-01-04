package edu.wpi.grip.ui.deployment;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.ui.util.deployment.DeployedInstanceManager;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.io.OutputStream;
import java.net.InetAddress;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public class FRCDeploymentOptionsController extends DeploymentOptionsController {

    private Spinner<Integer> teamNumberSpinner;
    private final DeployedInstanceManager.Factory deployedInstanceManagerFactor;
    private final Supplier<OutputStream> stdOut, stdErr;

    public interface Factory {
        FRCDeploymentOptionsController create(
                Consumer<DeployedInstanceManager> onDeployCallback,
                @Assisted("stdOut") Supplier<OutputStream> stdOut,
                @Assisted("stdErr") Supplier<OutputStream> stdErr
            );
    }

    @Inject
    FRCDeploymentOptionsController(DeployedInstanceManager.Factory deployedInstanceManagerFactor,
                                   @Assisted Consumer<DeployedInstanceManager> onDeployCallback,
                                   @Assisted("stdOut") Supplier<OutputStream> stdOut,
                                   @Assisted("stdErr") Supplier<OutputStream> stdErr) {
        super("FRC", onDeployCallback);
        this.deployedInstanceManagerFactor = deployedInstanceManagerFactor;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    @Override
    @SuppressWarnings("PMD.IfElseStmtsMustUseBraces")
    protected void postInit() {
        final Label label = new Label("Team Number");
        final SpinnerValueFactory.IntegerSpinnerValueFactory spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 190);
        this.teamNumberSpinner = new Spinner(spinnerValueFactory);
        this.teamNumberSpinner.setEditable(true);
        // Ensure the value entered is only a number
        this.teamNumberSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if ("".equals(newValue)) {
                teamNumberSpinner.getEditor().setText(Integer.toString(0));
            } else try {
                int value = Integer.parseInt(newValue);
                teamNumberSpinner.getEditor().setText(Integer.toString(value));
            } catch (NumberFormatException e) {
                teamNumberSpinner.getEditor().setText(oldValue);
            }
        });
        getOptionsGrid().addRow(0, label, this.teamNumberSpinner);
    }

    @Override
    protected Promise<DeployedInstanceManager, String, String> onDeploy() {
        final Deferred<DeployedInstanceManager, String, String> deferredDeploy = new DeferredObject<>();
        int teamNumber = checkNotNull(teamNumberSpinner.getValue(), "Team number can not be null");
        checkInetAddressReachable(() -> InetAddress.getByName("roborio-" + teamNumber + "-frc.local"))
                .progress(deferredDeploy::notify)
                .fail(throwable -> deferredDeploy.reject(throwable.getMessage()))
                .done(address -> deferredDeploy.resolve(deployedInstanceManagerFactor.createFRC(address, stdOut, stdErr)));
        return deferredDeploy.promise();
    }
}
