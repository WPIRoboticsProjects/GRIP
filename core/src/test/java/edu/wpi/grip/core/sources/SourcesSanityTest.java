package edu.wpi.grip.core.sources;


import edu.wpi.grip.core.MockPipeline;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.GripServerTest;
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

    GripServer.HttpServerFactory f = new GripServerTest.TestServerFactory();
    MockPipeline.MockProjectSettings projectSettings = new MockPipeline.MockProjectSettings();
    projectSettings.setServerPort(8080);
    GripServer server = GripServerTest.makeServer(f, new MockPipeline(projectSettings));
    setDefault(GripServer.class, server);
  }
}
