package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.util.GRIPCoreTestModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StepTest {
    private EventBus eventBus;
    private OperationMetaData additionMeta;
    private GRIPCoreTestModule testModule = new GRIPCoreTestModule();

    @Before
    public void setUp() {
        testModule.setUp();
        Injector injector = Guice.createInjector(testModule);
        InputSocket.Factory isf = injector.getInstance(InputSocket.Factory.class);
        OutputSocket.Factory osf = injector.getInstance(OutputSocket.Factory.class);
        additionMeta = new OperationMetaData(AdditionOperation.DESCRIPTION, () -> new AdditionOperation(isf, osf));
        eventBus = injector.getInstance(EventBus.class);
    }

    @After
    public void tearDown() {
        testModule.tearDown();
    }

    @Test(expected = NullPointerException.class)
    public void testOperationNotNull() {
        new Step.Factory((origin) -> null).create(null);
    }

    @Test
    public void testStep() {
        Step step = new Step.Factory((origin) -> new MockExceptionWitness(eventBus, origin)).create(additionMeta);
        Socket<Double> a = (Socket<Double>) step.getInputSockets().get(0);
        Socket<Double> b = (Socket<Double>) step.getInputSockets().get(1);
        Socket<Double> c = (Socket<Double>) step.getOutputSockets().get(0);

        a.setValue(1234.0);
        b.setValue(5678.0);
        step.runPerformIfPossible();

        assertEquals("Step did not perform correctly", (Double) (1234.0 + 5678.0), c.getValue().get());
        eventBus.unregister(step);
    }

    @Test
    public void testSocketDirection() {
        Step step = new Step.Factory((origin) -> new MockExceptionWitness(eventBus, origin)).create(additionMeta);
        Socket<Double> a = (Socket<Double>) step.getInputSockets().get(0);
        Socket<Double> b = (Socket<Double>) step.getInputSockets().get(1);
        Socket<Double> c = (Socket<Double>) step.getOutputSockets().get(0);

        assertEquals("Socket was not an input", Socket.Direction.INPUT, a.getDirection());
        assertEquals("Socket was not an input", Socket.Direction.INPUT, b.getDirection());
        assertEquals("Socket was not an output", Socket.Direction.OUTPUT, c.getDirection());
    }

    @Test
    public void testGetOperation() {
        Step step = new Step.Factory((origin) -> new MockExceptionWitness(eventBus, origin)).create(additionMeta);

        assertEquals("Operation descriptions were not the same", additionMeta.getDescription(), step.getOperationDescription());
    }
}
