package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.MockInputSocketFactory;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.sockets.OutputSocket;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OperationTest {
    private Operation addition;

    @Before
    public void setUp() {
        EventBus eventBus = new EventBus();
        InputSocket.Factory isf = new MockInputSocketFactory(eventBus);
        OutputSocket.Factory osf = new MockOutputSocketFactory(eventBus);
        addition = new AdditionOperation(isf, osf);
    }

    @Test
    public void testOperation() throws Exception {
        List<InputSocket> inputs = addition.getInputSockets();
        List<OutputSocket> outputs = addition.getOutputSockets();
        InputSocket<Double> a = inputs.get(0);
        InputSocket<Double> b = inputs.get(1);
        OutputSocket c = outputs.get(0);

        a.setValue(1234.0);
        b.setValue(5678.0);
        addition.perform();

        assertEquals((Double) (1234.0 + 5678.0), c.getValue().get());
    }
}
