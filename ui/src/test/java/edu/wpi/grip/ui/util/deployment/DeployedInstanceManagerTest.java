package edu.wpi.grip.ui.util.deployment;


import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;

import static junit.framework.TestCase.assertFalse;

public class DeployedInstanceManagerTest {

    private DeployedInstanceManager deployedInstanceManager;

    @Before
    public void setUp() throws URISyntaxException, IOException {
        File testSave = new File(DeployedInstanceManager.class.getResource("LoadThreeStepsTestSave.grip").toURI());
        deployedInstanceManager =
                new DeployedInstanceManager.Factory(new EventBus(), null, new SecureShellDetails.Factory(), new DeploymentCommands.Factory())
                        .createFRC(InetAddress.getByName("roborio-192-frc.local"), testSave);
    }


    @Test(timeout = 30000)
    @Ignore("This needs a RoboRIO attached to work")
    public void testThatThisWorks() throws IOException, InterruptedException {
        deployedInstanceManager.deploy().waitSafely();
        deployedInstanceManager.start();
        do {
            Thread.sleep(50);
        } while (!deployedInstanceManager.isStarted() && !Thread.interrupted());
        deployedInstanceManager.stop();
        assertFalse("The manager should have stopped after calling stop", deployedInstanceManager.isStarted());
    }
}