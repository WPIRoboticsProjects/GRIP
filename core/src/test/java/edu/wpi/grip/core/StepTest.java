package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.util.MockExceptionWitness;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StepTest {
    private EventBus eventBus;
    private Operation addition;

    @Before
    public void setUp() {
        this.eventBus = new EventBus();
        this.addition = new AdditionOperation();
    }

    @Test(expected = NullPointerException.class)
    public void testOperationNotNull() {
        new Step.Factory(eventBus, (origin) -> null).create(null);
    }

    @Test
    public void testStep() {
        Step step = new Step.Factory(eventBus, (origin) -> new MockExceptionWitness(eventBus, origin)).create(addition);
        Socket<Double> a = (Socket<Double>) step.getInputSockets()[0];
        Socket<Double> b = (Socket<Double>) step.getInputSockets()[1];
        Socket<Double> c = (Socket<Double>) step.getOutputSockets()[0];

        a.setValue(1234.0);
        b.setValue(5678.0);
        step.runPerformIfPossible();

        assertEquals((Double) (1234.0 + 5678.0), c.getValue().get());
        eventBus.unregister(step);
    }

    @Test
    public void testSocketDirection() {
        Step step = new Step.Factory(eventBus, (origin) -> new MockExceptionWitness(eventBus, origin)).create(addition);
        Socket<Double> a = (Socket<Double>) step.getInputSockets()[0];
        Socket<Double> b = (Socket<Double>) step.getInputSockets()[1];
        Socket<Double> c = (Socket<Double>) step.getOutputSockets()[0];

        assertEquals(Socket.Direction.INPUT, a.getDirection());
        assertEquals(Socket.Direction.INPUT, b.getDirection());
        assertEquals(Socket.Direction.OUTPUT, c.getDirection());
    }

    @Test
    public void testGetOperation() {
        Step step = new Step.Factory(eventBus, (origin) -> new MockExceptionWitness(eventBus, origin)).create(addition);

        assertEquals(addition, step.getOperation());
    }
}
