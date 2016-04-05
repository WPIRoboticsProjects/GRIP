package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.util.GRIPCoreTestModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OperationTest {
    private EventBus eventBus = new EventBus();
    private Operation addition;
    private GRIPCoreTestModule testModule = new GRIPCoreTestModule();

    @Before
    public void setUp() {
        testModule.setUp();
        Injector injector = Guice.createInjector(testModule);
        InputSocket.Factory isf = injector.getInstance(InputSocket.Factory.class);
        OutputSocket.Factory osf = injector.getInstance(OutputSocket.Factory.class);
        addition = new AdditionOperation(isf, osf);
    }

    @After
    public void tearDown() {
        testModule.tearDown();
    }

    @Test
    public void testOperation() throws Exception {
        List<InputSocket> inputs = addition.getInputSockets();
        List<OutputSocket> outputs = addition.getOutputSockets();
        InputSocket<Double> a = inputs.get(0), b = inputs.get(1);
        OutputSocket c = outputs.get(0);

        a.setValue(1234.0);
        b.setValue(5678.0);
        addition.perform();

        assertEquals((Double) (1234.0 + 5678.0), c.getValue().get());
    }
}
