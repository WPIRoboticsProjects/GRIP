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
        Socket<Double> term1 = (Socket<Double>) step.getInputSockets()[0];
        Socket<Double> term2 = (Socket<Double>) step.getInputSockets()[1];
        Socket<Double> expectedSum = (Socket<Double>) step.getOutputSockets()[0];

        term1.setValue(1234.0);
        term2.setValue(5678.0);
        assertEquals((Double) (1234.0 + 5678.0), expectedSum.getValue());

        eventBus.unregister(step);
    }
}
