package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import org.junit.Test;

import static org.junit.Assert.*;

public class OperationTest {
    EventBus eventBus = new EventBus();
    Operation addition = new AdditionOperation();

    @Test
    public void testOperation() throws Exception {
        Socket[] inputs = addition.createInputSockets(eventBus);
        Socket[] outputs = addition.createOutputSockets(eventBus);
        Socket<Double> term1 = inputs[0], term2 = inputs[1], sum = outputs[0];

        term1.setValue(1234.0);
        term2.setValue(5678.0);
        addition.perform(inputs, outputs);

        assertEquals((Double) (1234.0 + 5678.0), sum.getValue());
    }
}
