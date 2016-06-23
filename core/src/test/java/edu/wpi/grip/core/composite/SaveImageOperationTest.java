package edu.wpi.grip.core.composite;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import edu.wpi.grip.core.FileManager;
import edu.wpi.grip.core.operations.composite.SaveImageOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.util.GRIPCoreTestModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SaveImageOperationTest {

    private GRIPCoreTestModule testModule;

    private InputSocket<Boolean> activeSocket;

    @Inject
    private InputSocket.Factory isf;

    @Inject
    private OutputSocket.Factory osf;

    @Inject
    private FileManager fileManager;

    @Before
    public void setUp() throws Exception {
        testModule = new GRIPCoreTestModule();
        testModule.setUp();

        final Injector injector = Guice.createInjector(testModule);
        injector.injectMembers(this);
        SaveImageOperation operation = new SaveImageOperation(isf, osf, fileManager);
        activeSocket = operation.getInputSockets().stream().filter(o -> o.getSocketHint().getIdentifier().equals("Active") && o.getSocketHint().getType().equals(Boolean.class)).findFirst().get();
    }

    @After
    public void tearDown() {
        testModule.tearDown();
    }

    @Test
    public void testActiveButtonDefaultsDisabled() {
        assertFalse("The active socket was not false (disabled).", activeSocket.getValue().get());
    }
}
