package edu.wpi.grip.ui.deployment;


import edu.wpi.grip.ui.util.deployment.DeployedInstanceManager;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DeploymentOptionsControllersFactory {

    private final DeployedInstanceManager.Factory deployedInstanceManagerFactory;

    public DeploymentOptionsControllersFactory(DeployedInstanceManager.Factory deployedInstanceManagerFactory) {
        this.deployedInstanceManagerFactory = deployedInstanceManagerFactory;
    }


    public Collection<DeploymentOptionsController> createControllers(Consumer<DeployedInstanceManager> onDeployCallback, Supplier<OutputStream> stdOut, Supplier<OutputStream> stdErr) {
        return Arrays.asList(
                new FRCDeploymentOptionsController(deployedInstanceManagerFactory, onDeployCallback, stdOut, stdErr),
                new FRCAdvancedDeployment(deployedInstanceManagerFactory, onDeployCallback, stdOut, stdErr)
        );
    }
}
