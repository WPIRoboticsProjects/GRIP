package edu.wpi.grip.core;

import com.google.common.testing.AbstractPackageSanityTests;
import com.google.common.util.concurrent.Service;
import edu.wpi.grip.core.sockets.*;
import edu.wpi.grip.core.util.service.SingleActionListener;

import java.util.Arrays;

public class CoreSanityTest extends AbstractPackageSanityTests {

    public CoreSanityTest() {
        super();
        publicApiOnly();
        ignoreClasses(c -> c.getName().contains("Mock"));
        ignoreClasses(c -> Arrays.asList(
                AdditionOperation.class,
                AddOperation.class,
                ManualPipelineRunner.class,
                SubtractionOperation.class,
                Main.class
        ).contains(c));
        setDefault(OutputSocket.class, new MockOutputSocket("Mock Out"));
        setDefault(InputSocket.class, new MockInputSocket("Mock In"));
        setDefault(SocketHint.class, SocketHints.createBooleanSocketHint("Mock bool", false));
        setDistinctValues(Socket.class, new MockInputSocket("Mock In Socket"), new MockOutputSocket("Mock Out Socket"));
        setDefault(Service.Listener.class, new SingleActionListener(() -> {
        }));
        setDefault(ConnectionValidator.class, (outputSocket, inputSocket) -> true);
        setDefault(OperationMetaData.class,
                new OperationMetaData(OperationDescription.builder().name("").summary("").build(),
                () -> null));
        setDefault(OperationDescription.class, OperationDescription.builder().name("").summary("").build());
    }
}
