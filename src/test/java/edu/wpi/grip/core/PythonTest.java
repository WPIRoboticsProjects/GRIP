package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.operations.PythonScriptOperation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PythonTest {
    static final int a = 1234, b = 5678;

    EventBus eventBus = new EventBus();

    @Test
    public void testPython() throws Exception {
        Operation addition = new PythonScriptOperation(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition.py"));
        Step step = new Step(eventBus, addition);
        Socket aSocket = step.getInputSockets()[0];
        Socket bSocket = step.getInputSockets()[1];
        Socket sumSocket = step.getOutputSockets()[0];

        aSocket.setValue(a);
        bSocket.setValue(b);

        assertEquals(a + b, sumSocket.getValue());
    }

    @Test
    public void testPythonAdditionFromString() throws Exception {
        Operation additionFromString = new PythonScriptOperation("import edu.wpi.grip.core as grip\nimport java" +
                ".lang.Integer\n\ninputs = [\n    grip.SocketHint(\"a\", java.lang.Integer, 0),\n    grip.SocketHint(" +
                "\"b\", java.lang.Integer, 0),\n]\n\noutputs = [\n    grip.SocketHint(\"sum\", java.lang.Integer, 0)," +
                "\n]\n\ndef perform(a, b):\n    return a + b\n");
        Step step = new Step(eventBus, additionFromString);
        Socket aSocket = step.getInputSockets()[0];
        Socket bSocket = step.getInputSockets()[1];
        Socket sumSocket = step.getOutputSockets()[0];

        aSocket.setValue(a);
        bSocket.setValue(b);

        assertEquals(a + b, sumSocket.getValue());
    }

    @Test
    public void testPythonMultipleOutputs() throws Exception {
        Operation additionSubtraction = new PythonScriptOperation(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition-subtraction.py"));
        Step step = new Step(eventBus, additionSubtraction);
        Socket aSocket = step.getInputSockets()[0];
        Socket bSocket = step.getInputSockets()[1];
        Socket sumSocket = step.getOutputSockets()[0];
        Socket differenceSocket = step.getOutputSockets()[1];

        aSocket.setValue(a);
        bSocket.setValue(b);

        assertEquals(a + b, sumSocket.getValue());
        assertEquals(a - b, differenceSocket.getValue());
    }

    @Test
    public void testPythonWrongOutputCount() throws Exception {
        Operation additionWrongOutputCount = new PythonScriptOperation(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition-wrong-output-count.py"));
        Step step = new Step(eventBus, additionWrongOutputCount);
        Socket aSocket = step.getInputSockets()[0];
        Socket bSocket = step.getInputSockets()[1];
        Socket sumSocket = step.getOutputSockets()[0];

        aSocket.setValue(a);
        bSocket.setValue(b);

        assertEquals(0, sumSocket.getValue());
    }

    @Test
    public void testPythonWrongOutputType() throws Exception {
        Operation additionWrongOutputType = new PythonScriptOperation(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition-wrong-output-type.py"));
        Step step = new Step(eventBus, additionWrongOutputType);
        Socket aSocket = step.getInputSockets()[0];
        Socket bSocket = step.getInputSockets()[1];
        Socket sumSocket = step.getOutputSockets()[0];

        aSocket.setValue(a);
        bSocket.setValue(b);

        assertEquals(0, sumSocket.getValue());
    }

    @Test
    public void testDefaultName() throws Exception {
        Operation addition = new PythonScriptOperation(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition.py"));
        assertEquals("addition.py", addition.getName());
    }

    @Test
    public void testDefaultDescription() throws Exception {
        Operation addition = new PythonScriptOperation(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition.py"));
        assertEquals("", addition.getDescription());
    }

    @Test
    public void testName() throws Exception {
        Operation addition = new PythonScriptOperation(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition-with-name-and-description.py"));
        assertEquals("Add", addition.getName());
    }

    @Test
    public void testDescription() throws Exception {
        Operation addition = new PythonScriptOperation(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition-with-name-and-description.py"));
        assertEquals("Compute the sum of two integers", addition.getDescription());
    }
}
