package edu.wpi.grip.ui.deployment;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.wpi.grip.ui.util.deployment.DeployedInstanceManager;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Singleton
public class DeploymentOptionsControllersFactory {

    private final FRCDeploymentOptionsController.Factory frcDeploymentOptionsControllerFactory;
    private final FRCAdvancedDeploymentOptionsController.Factory frcAdvancedDeploymentOptionsControllerFactory;

    @Inject
    DeploymentOptionsControllersFactory(
            FRCDeploymentOptionsController.Factory frcDeploymentOptionsControllerFactory,
            FRCAdvancedDeploymentOptionsController.Factory frcAdvancedDeploymentOptionsControllerFactory) {
        this.frcDeploymentOptionsControllerFactory = frcDeploymentOptionsControllerFactory;
        this.frcAdvancedDeploymentOptionsControllerFactory = frcAdvancedDeploymentOptionsControllerFactory;
    }


    public Collection<DeploymentOptionsController> createControllers(Consumer<DeployedInstanceManager> onDeployCallback, Supplier<OutputStream> stdOut, Supplier<OutputStream> stdErr) {
        return Arrays.asList(
                frcDeploymentOptionsControllerFactory.create(onDeployCallback, stdOut, stdErr),
                frcAdvancedDeploymentOptionsControllerFactory.create(onDeployCallback, stdOut, stdErr)
        );
    }
}
