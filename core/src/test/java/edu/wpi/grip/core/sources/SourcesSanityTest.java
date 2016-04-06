package edu.wpi.grip.core.sources;


import com.google.common.testing.AbstractPackageSanityTests;
import com.google.common.util.concurrent.Service;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.core.util.service.SingleActionListener;

import java.util.Arrays;

public class SourcesSanityTest extends AbstractPackageSanityTests {
    public SourcesSanityTest() {
        super();
        publicApiOnly();
        ignoreClasses(c -> c.getName().contains("Mock"));
        ignoreClasses(c-> Arrays.asList(IPCameraFrameGrabber.class).contains(c));
        setDefault(Service.Listener.class, new SingleActionListener(() -> {}));
        setDefault(ExceptionWitness.Factory.class, MockExceptionWitness.MOCK_FACTORY);
    }
}
