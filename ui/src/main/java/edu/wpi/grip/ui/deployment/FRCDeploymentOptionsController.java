package edu.wpi.grip.ui.deployment;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.ui.util.Spinners;
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
    private final int teamNumber;
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
                                   Pipeline pipeline,
                                   @Assisted Consumer<DeployedInstanceManager> onDeployCallback,
                                   @Assisted("stdOut") Supplier<OutputStream> stdOut,
                                   @Assisted("stdErr") Supplier<OutputStream> stdErr) {
        super("FRC", onDeployCallback);
        this.deployedInstanceManagerFactor = deployedInstanceManagerFactor;
        this.teamNumber = pipeline.getProjectSettings().getTeamNumber();
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    @Override
    protected void postInit() {
        final Label label = new Label("Team Number");
        final SpinnerValueFactory.IntegerSpinnerValueFactory spinnerValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, teamNumber);
        this.teamNumberSpinner = new Spinner(spinnerValueFactory);
        Spinners.makeEditableSafely(teamNumberSpinner, Integer::valueOf);
        label.setLabelFor(teamNumberSpinner);

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
