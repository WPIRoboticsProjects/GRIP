package edu.wpi.grip.core.operations;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.Step;
import org.junit.Test;
import org.junit.BeforeClass;
import org.python.core.PySystemState;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PythonScriptOperationTest {
    static final int TERM1 = 1234, TERM2 = 5678;

    EventBus eventBus = new EventBus();
    Operation additionFromString = new PythonScriptOperation("import edu.wpi.grip.core as grip\nimport java.lang.Integer\n\ninputs = [\n    grip.SocketHint(\"TERM1\", java.lang.Integer),\n    grip.SocketHint(\"TERM2\", java.lang.Integer),\n]\n\noutputs = [\n    grip.SocketHint(\"c\", java.lang.Integer),\n]\n\ndef perform(TERM1, TERM2):\n    return TERM1 + TERM2\n");
    Operation addition = new PythonScriptOperation(PythonScriptOperation.class.getResourceAsStream("/edu/wpi/grip/scripts/addition.py"));
    Operation additionSubtraction = new PythonScriptOperation(PythonScriptOperation.class.getResourceAsStream("/edu/wpi/grip/scripts/addition-subtraction.py"));
    Operation additionWrongOutputCount = new PythonScriptOperation(PythonScriptOperation.class.getResourceAsStream("/edu/wpi/grip/scripts/addition-wrong-output-count.py"));
    Operation additionWrongOutputType = new PythonScriptOperation(PythonScriptOperation.class.getResourceAsStream("/edu/wpi/grip/scripts/addition-wrong-output-type.py"));

    @BeforeClass
    public static void setupPythonProperties() {
        Properties pythonProperties = new Properties();
        pythonProperties.setProperty("python.import.site", "false");
        PySystemState.initialize(pythonProperties, null);
    }

    @Test
    public void testPython() throws Exception {
        Step step = new Step(eventBus, addition);
        Socket aSocket = step.getInputSockets()[0];
        Socket bSocket = step.getInputSockets()[1];
        Socket sumSocket = step.getOutputSockets()[0];

        aSocket.setValue(TERM1);
        bSocket.setValue(TERM2);

        assertEquals(TERM1 + TERM2, sumSocket.getValue());
    }

    @Test
    public void testPythonAdditionFromString() throws Exception {
        Step step = new Step(eventBus, additionFromString);
        Socket aSocket = step.getInputSockets()[0];
        Socket bSocket = step.getInputSockets()[1];
        Socket sumSocket = step.getOutputSockets()[0];

        aSocket.setValue(TERM1);
        bSocket.setValue(TERM2);

        assertEquals(TERM1 + TERM2, sumSocket.getValue());
    }

    @Test
    public void testPythonMultipleOutputs() throws Exception {
        Step step = new Step(eventBus, additionSubtraction);
        Socket aSocket = step.getInputSockets()[0];
        Socket bSocket = step.getInputSockets()[1];
        Socket sumSocket = step.getOutputSockets()[0];
        Socket differenceSocket = step.getOutputSockets()[1];

        aSocket.setValue(TERM1);
        bSocket.setValue(TERM2);

        assertEquals(TERM1 + TERM2, sumSocket.getValue());
        assertEquals(TERM1 - TERM2, differenceSocket.getValue());
    }

    @Test
    public void testPythonWrongOutputCount() throws Exception {
        Step step = new Step(eventBus, additionWrongOutputCount);
        Socket aSocket = step.getInputSockets()[0];
        Socket bSocket = step.getInputSockets()[1];
        Socket sumSocket = step.getOutputSockets()[0];

        aSocket.setValue(TERM1);
        bSocket.setValue(TERM2);

        assertNull(sumSocket.getValue());
    }

    @Test
    public void testPythonWrongOutputType() throws Exception {
        Step step = new Step(eventBus, additionWrongOutputType);
        Socket aSocket = step.getInputSockets()[0];
        Socket bSocket = step.getInputSockets()[1];
        Socket sumSocket = step.getOutputSockets()[0];

        aSocket.setValue(TERM1);
        bSocket.setValue(TERM2);

        assertNull(sumSocket.getValue());
    }
}
