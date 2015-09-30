package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import org.junit.Test;

import static org.junit.Assert.*;

public class StepTest {
    EventBus eventBus = new EventBus();
    Operation addition = new AdditionOperation();

    @Test(expected = NullPointerException.class)
    public void testEventBusNotNull() {
        new Step(null, addition);
    }

    @Test(expected = NullPointerException.class)
    public void testOperationNotNull() {
        new Step(eventBus, null);
    }

    @Test
    public void testStep() {
        Step step = new Step(eventBus, addition);
        Socket<Double> a = (Socket<Double>) step.getInputSockets()[0];
        Socket<Double> b = (Socket<Double>) step.getInputSockets()[1];
        Socket<Double> c = (Socket<Double>) step.getOutputSockets()[0];

        a.setValue(1234.0);
        b.setValue(5678.0);
        assertEquals((Double) (1234.0 + 5678.0), c.getValue());

        eventBus.unregister(step);
    }

    @Test
    public void testSocketDirection() {
        Step step = new Step(eventBus, addition);
        Socket<Double> a = (Socket<Double>) step.getInputSockets()[0];
        Socket<Double> b = (Socket<Double>) step.getInputSockets()[1];
        Socket<Double> c = (Socket<Double>) step.getOutputSockets()[0];

        assertEquals(Socket.Direction.INPUT, a.getDirection());
        assertEquals(Socket.Direction.INPUT, b.getDirection());
        assertEquals(Socket.Direction.OUTPUT, c.getDirection());
    }

    @Test
    public void testGetOperation() {
        Step step = new Step(eventBus, addition);

        assertEquals(addition, step.getOperation());
    }
}
