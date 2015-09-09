package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.StepAddedEvent;
import edu.wpi.grip.core.operations.PythonScriptOperation;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SerializationTest {
    private final EventBus eventBus = new EventBus();
    private final Serialization serialization = new Serialization(eventBus);

    private final Operation additionOperation, pythonAdditionOperationFromURL, pythonAdditionOperationFromSource;

    public SerializationTest() throws Exception {
        additionOperation = new AdditionOperation();
        pythonAdditionOperationFromURL = new PythonScriptOperation(
                SerializationTest.class.getResource("/edu/wpi/grip/scripts/addition.py"));
        pythonAdditionOperationFromSource = new PythonScriptOperation("import edu.wpi.grip.core as grip\nimport java" +
                ".lang.Integer\n\ninputs = [\n    grip.SocketHint(\"a\", java.lang.Integer, grip.SocketHint.View.NON" +
                "E, None, 0),\n    grip.SocketHint(\"b\", java.lang.Integer, grip.SocketHint.View.NONE, None, 0),\n]" +
                "\n\noutputs = [\n    grip.SocketHint(\"c\", java.lang.Integer),\n]\n\ndef perform(a, b):\n    retur" +
                "n a + b\n");
    }

    private Pipeline serializeAndDeserialize(Pipeline pipeline) {
        Writer writer = new StringWriter();
        serialization.savePipeline(pipeline, writer);

        Reader reader = new StringReader(writer.toString());
        return serialization.loadPipeline(reader);
    }

    @Test
    public void testSerializeEmptyPipeline() throws Exception {
        Pipeline pipeline1 = new Pipeline(eventBus);

        Pipeline pipeline2 = serializeAndDeserialize(pipeline1);

        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                0, pipeline2.getSteps().size());
        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                0, pipeline2.getConnections().size());
    }

    @Test
    public void testSerializePipelineWithSteps() throws Exception {
        Pipeline pipeline1 = new Pipeline(eventBus);
        eventBus.post(new StepAddedEvent(new Step(eventBus, additionOperation)));
        eventBus.post(new StepAddedEvent(new Step(eventBus, pythonAdditionOperationFromSource)));
        eventBus.post(new StepAddedEvent(new Step(eventBus, pythonAdditionOperationFromURL)));

        Pipeline pipeline2 = serializeAndDeserialize(pipeline1);

        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                3, pipeline2.getSteps().size());
        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                0, pipeline2.getConnections().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerializePipelineWithStepsAndConnections() throws Exception {
        Pipeline pipeline1 = new Pipeline(eventBus);

        Step step1 = new Step(eventBus, pythonAdditionOperationFromSource);
        Socket<Integer> a1 = (Socket<Integer>) step1.getInputSockets()[0];
        Socket<Integer> b1 = (Socket<Integer>) step1.getInputSockets()[1];
        Socket<Integer> sum1 = (Socket<Integer>) step1.getOutputSockets()[0];

        Step step2 = new Step(eventBus, pythonAdditionOperationFromURL);
        Socket<Integer> a2 = (Socket<Integer>) step2.getInputSockets()[0];
        Socket<Integer> b2 = (Socket<Integer>) step2.getInputSockets()[1];
        Socket<Integer> sum2 = (Socket<Integer>) step2.getOutputSockets()[0];

        a1.setValue(12);
        b1.setValue(34);
        b2.setValue(56);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new ConnectionAddedEvent(new Connection<Integer>(eventBus, sum1, a2)));

        Pipeline pipeline2 = serializeAndDeserialize(pipeline1);

        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                2, pipeline2.getSteps().size());
        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                1, pipeline2.getConnections().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedStep() throws Exception {
        Pipeline pipeline1 = new Pipeline(eventBus);
        eventBus.post(new StepAddedEvent(new Step(eventBus, additionOperation)));
        Pipeline pipeline2 = serializeAndDeserialize(pipeline1);

        Socket<Double> a = (Socket<Double>) pipeline2.getSteps().get(0).getInputSockets()[0];
        Socket<Double> b = (Socket<Double>) pipeline2.getSteps().get(0).getInputSockets()[1];
        Socket<Double> sum = (Socket<Double>) pipeline2.getSteps().get(0).getOutputSockets()[0];
        a.setValue(123.4);
        b.setValue(567.8);

        assertEquals((Double) (123.4 + 567.8), sum.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPythonStepFromURL() throws Exception {
        Pipeline pipeline1 = new Pipeline(eventBus);
        eventBus.post(new StepAddedEvent(new Step(eventBus, pythonAdditionOperationFromURL)));
        Pipeline pipeline2 = serializeAndDeserialize(pipeline1);

        Socket<Integer> a = (Socket<Integer>) pipeline2.getSteps().get(0).getInputSockets()[0];
        Socket<Integer> b = (Socket<Integer>) pipeline2.getSteps().get(0).getInputSockets()[1];
        Socket<Integer> sum = (Socket<Integer>) pipeline2.getSteps().get(0).getOutputSockets()[0];
        a.setValue(1234);
        b.setValue(5678);

        assertEquals((Integer) (1234 + 5678), sum.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPythonStepFromSource() throws Exception {
        Pipeline pipeline1 = new Pipeline(eventBus);
        eventBus.post(new StepAddedEvent(new Step(eventBus, pythonAdditionOperationFromSource)));
        Pipeline pipeline2 = serializeAndDeserialize(pipeline1);

        Socket<Integer> a = (Socket<Integer>) pipeline2.getSteps().get(0).getInputSockets()[0];
        Socket<Integer> b = (Socket<Integer>) pipeline2.getSteps().get(0).getInputSockets()[1];
        Socket<Integer> sum = (Socket<Integer>) pipeline2.getSteps().get(0).getOutputSockets()[0];
        a.setValue(1234);
        b.setValue(5678);

        assertEquals((Integer) (1234 + 5678), sum.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPipeline() throws Exception {
        Pipeline pipeline1 = new Pipeline(eventBus);
        Step step1 = new Step(eventBus, pythonAdditionOperationFromURL);
        Step step2 = new Step(eventBus, pythonAdditionOperationFromSource);
        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new ConnectionAddedEvent(new Connection(eventBus, step1.getOutputSockets()[0],
                step2.getInputSockets()[0])));
        Pipeline pipeline2 = serializeAndDeserialize(pipeline1);

        Socket<Integer> a1 = (Socket<Integer>) pipeline2.getSteps().get(0).getInputSockets()[0];
        Socket<Integer> b1 = (Socket<Integer>) pipeline2.getSteps().get(0).getInputSockets()[1];
        Socket<Integer> b2 = (Socket<Integer>) pipeline2.getSteps().get(1).getInputSockets()[1];
        Socket<Integer> sum2 = (Socket<Integer>) pipeline2.getSteps().get(1).getOutputSockets()[0];
        a1.setValue(123);
        b1.setValue(456);
        b2.setValue(789);

        assertEquals((Integer) (123 + 456 + 789), sum2.getValue());
    }
}
