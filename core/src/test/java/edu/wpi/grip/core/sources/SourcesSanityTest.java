package edu.wpi.grip.core.sources;


import edu.wpi.grip.core.http.ContextStore;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.GripServerTest;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.core.util.service.SingleActionListener;

import com.google.common.testing.AbstractPackageSanityTests;
import com.google.common.util.concurrent.Service;

import java.util.Arrays;

public class SourcesSanityTest extends AbstractPackageSanityTests {
  public SourcesSanityTest() {
    super();
    publicApiOnly();
    ignoreClasses(c -> c.getName().contains("Mock"));
    ignoreClasses(c -> Arrays.asList(IPCameraFrameGrabber.class, HttpSource.class).contains(c));
    setDefault(Service.Listener.class, new SingleActionListener(() -> {
    }));
    setDefault(ExceptionWitness.Factory.class, MockExceptionWitness.MOCK_FACTORY);

    GripServer.JettyServerFactory serverFactory = new GripServerTest.TestServerFactory();
    ProjectSettings projectSettings = new ProjectSettings();
    projectSettings.setServerPort(8080);
    GripServer server = GripServerTest.makeServer(new ContextStore(), serverFactory, () -> projectSettings);
    setDefault(GripServer.class, server);
  }
}
