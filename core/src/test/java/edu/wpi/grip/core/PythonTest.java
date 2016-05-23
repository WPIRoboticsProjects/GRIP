package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.operations.PythonScriptFile;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.util.GRIPCoreTestModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PythonTest {
    private static final int a = 1234, b = 5678;

    private GRIPCoreTestModule testModule;
    private EventBus eventBus;
    private InputSocket.Factory isf;
    private OutputSocket.Factory osf;

    @Before
    public void setUp() {
        testModule = new GRIPCoreTestModule();
        testModule.setUp();
        final Injector injector = Guice.createInjector(testModule);
        eventBus = injector.getInstance(EventBus.class);
        isf = injector.getInstance(InputSocket.Factory.class);
        osf = injector.getInstance(OutputSocket.Factory.class);
    }

    @Test
    public void testPython() throws Exception {
        PythonScriptFile pythonScriptFile = PythonScriptFile.create(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition.py"));
        Step step = new Step.Factory((origin) -> new MockExceptionWitness(eventBus, origin)).create(pythonScriptFile.toOperationMetaData(isf, osf));
        Socket aSocket = step.getInputSockets().get(0);
        Socket bSocket = step.getInputSockets().get(1);
        Socket sumSocket = step.getOutputSockets().get(0);

        aSocket.setValue(a);
        bSocket.setValue(b);

        step.runPerformIfPossible();

        assertEquals("Value was not assigned after run", a + b, sumSocket.getValue().get());
    }

    @Test
    public void testPythonAdditionFromString() throws Exception {
        PythonScriptFile pythonScriptFile = PythonScriptFile.create("import edu.wpi.grip.core.sockets as grip\nimport java" +
                ".lang.Integer\n\nname = \"Addition Operation\"\n\ninputs = [\n    grip.SocketHints.createNumberSocketHint(\"a\", 0.0),\n    grip.SocketHints.createNumberSocketHint(" +
                "\"b\", 0.0),\n]\n\noutputs = [\n    grip.SocketHints.Outputs.createNumberSocketHint(\"sum\", 0.0)," +
                "\n]\n\ndef perform(a, b):\n    return a + b\n");
        Step step = new Step.Factory((origin) -> new MockExceptionWitness(eventBus, origin)).create(pythonScriptFile.toOperationMetaData(isf, osf));
        Socket aSocket = step.getInputSockets().get(0);
        Socket bSocket = step.getInputSockets().get(1);
        Socket sumSocket = step.getOutputSockets().get(0);


        aSocket.setValue(a);
        bSocket.setValue(b);

        step.runPerformIfPossible();

        assertEquals("Value was not assigned after run", a + b, sumSocket.getValue().get());
    }

    @Test
    public void testPythonMultipleOutputs() throws Exception {
        Step step = new Step.Factory((origin) -> new MockExceptionWitness(eventBus, origin)).create(
                PythonScriptFile.create(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition-subtraction.py")).toOperationMetaData(isf, osf)
        );
        Socket aSocket = step.getInputSockets().get(0);
        Socket bSocket = step.getInputSockets().get(1);
        Socket sumSocket = step.getOutputSockets().get(0);
        Socket differenceSocket = step.getOutputSockets().get(1);

        aSocket.setValue(a);
        bSocket.setValue(b);

        step.runPerformIfPossible();

        assertEquals("Value was not assigned after run", a + b, sumSocket.getValue().get());
        assertEquals("Value was not assigned after run", a - b, differenceSocket.getValue().get());
    }

    @Test
    public void testPythonWrongOutputCount() throws Exception {
        Step step = new Step.Factory((origin) -> new MockExceptionWitness(eventBus, origin)).create(
                PythonScriptFile.create(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition-wrong-output-count.py")).toOperationMetaData(isf, osf)
        );
        Socket aSocket = step.getInputSockets().get(0);
        Socket bSocket = step.getInputSockets().get(1);
        Socket sumSocket = step.getOutputSockets().get(0);

        aSocket.setValue(a);
        bSocket.setValue(b);

        assertEquals(0.0, sumSocket.getValue().get());
    }

    @Test
    public void testPythonWrongOutputType() throws Exception {
        Step step = new Step.Factory((origin) -> new MockExceptionWitness(eventBus, origin)).create(
                PythonScriptFile.create(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition-wrong-output-type.py")).toOperationMetaData(isf, osf));
        Socket aSocket = step.getInputSockets().get(0);
        Socket bSocket = step.getInputSockets().get(1);
        Socket sumSocket = step.getOutputSockets().get(0);

        aSocket.setValue(a);
        bSocket.setValue(b);

        step.runPerformIfPossible();

        assertEquals("Value was not assigned after run", 0.0, sumSocket.getValue().get());
    }

    @Test
    public void testDefaultName() throws Exception {
        OperationDescription additionDescription = PythonScriptFile.create(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition.py"))
                .toOperationMetaData(isf, osf).getDescription();
        assertEquals("Name incorrect", "addition.py", additionDescription.name());
    }

    @Test
    public void testDefaultDescription() throws Exception {
        OperationDescription additionDescription = PythonScriptFile.create(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition.py"))
                .toOperationMetaData(isf, osf).getDescription();
        assertEquals("Description incorrect", "", additionDescription.summary());
    }

    @Test
    public void testName() throws Exception {
        OperationDescription additionDescription = PythonScriptFile.create(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition-with-name-and-description.py"))
                .toOperationMetaData(isf, osf).getDescription();
        assertEquals("Name incorrect", "Add", additionDescription.name());
    }

    @Test
    public void testSummary() throws Exception {
        OperationDescription additionDescription = PythonScriptFile.create(PythonTest.class.getResource("/edu/wpi/grip/scripts/addition-with-name-and-description.py"))
                .toOperationMetaData(isf, osf).getDescription();
        assertEquals("Description incorrect", "Compute the sum of two integers", additionDescription.summary());
    }

    @After
    public void tearDown() {
        testModule.tearDown();
    }
}
