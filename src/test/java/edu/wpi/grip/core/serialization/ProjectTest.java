package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.events.StepAddedEvent;
import edu.wpi.grip.core.operations.PythonScriptOperation;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static junit.framework.TestCase.assertEquals;
import static org.bytedeco.javacpp.opencv_core.*;

public class ProjectTest {
    private final EventBus eventBus = new EventBus();

    private final Pipeline pipeline = new Pipeline(eventBus);
    private final Palette palette = new Palette(eventBus);
    private final Project project = new Project(eventBus, pipeline, palette);

    private final Operation additionOperation, opencvAddOperation, pythonAdditionOperationFromURL,
            pythonAdditionOperationFromSource;

    public ProjectTest() throws Exception {
        additionOperation = new AdditionOperation();
        pythonAdditionOperationFromURL = new PythonScriptOperation(
                ProjectTest.class.getResource("/edu/wpi/grip/scripts/addition.py"));
        pythonAdditionOperationFromSource = new PythonScriptOperation("import edu.wpi.grip.core as grip\nimport java" +
                ".lang.Integer\n\ninputs = [\n    grip.SocketHint(\"a\", java.lang.Integer, 0),\n    grip.SocketHint(" +
                "\"b\", java.lang.Integer, 0),\n]\n\noutputs = [\n    grip.SocketHint(\"sum\", java.lang.Integer, 0)," +
                "\n]\n\ndef perform(a, b):\n    return a + b\n");
        opencvAddOperation = new AddOperation();

        eventBus.post(new OperationAddedEvent(additionOperation));
        eventBus.post(new OperationAddedEvent(pythonAdditionOperationFromURL));
        eventBus.post(new OperationAddedEvent(pythonAdditionOperationFromSource));
        eventBus.post(new OperationAddedEvent(opencvAddOperation));
    }

    private void serializeAndDeserialize() {
        final Writer writer = new StringWriter();
        project.save(writer);

        final Reader reader = new StringReader(writer.toString());
        project.open(reader);
    }

    @Test
    public void testSerializeEmptyPipeline() throws Exception {
        serializeAndDeserialize();

        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                0, pipeline.getSteps().size());
        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                0, pipeline.getConnections().size());
    }

    @Test
    public void testSerializePipelineWithSteps() throws Exception {
        eventBus.post(new StepAddedEvent(new Step(eventBus, additionOperation)));
        eventBus.post(new StepAddedEvent(new Step(eventBus, pythonAdditionOperationFromSource)));
        eventBus.post(new StepAddedEvent(new Step(eventBus, pythonAdditionOperationFromURL)));

        serializeAndDeserialize();

        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                3, pipeline.getSteps().size());
        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                0, pipeline.getConnections().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerializePipelineWithStepsAndConnections() throws Exception {
        Step step1 = new Step(eventBus, pythonAdditionOperationFromSource);
        InputSocket<Integer> a1 = (InputSocket<Integer>) step1.getInputSockets()[0];
        InputSocket<Integer> b1 = (InputSocket<Integer>) step1.getInputSockets()[1];
        OutputSocket<Integer> sum1 = (OutputSocket<Integer>) step1.getOutputSockets()[0];

        Step step2 = new Step(eventBus, pythonAdditionOperationFromURL);
        InputSocket<Integer> a2 = (InputSocket<Integer>) step2.getInputSockets()[0];
        InputSocket<Integer> b2 = (InputSocket<Integer>) step2.getInputSockets()[1];
        OutputSocket<Integer> sum2 = (OutputSocket<Integer>) step2.getOutputSockets()[0];

        a1.setValue(12);
        b1.setValue(34);
        b2.setValue(56);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new ConnectionAddedEvent(new Connection<Integer>(eventBus, sum1, a2)));

        serializeAndDeserialize();

        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                2, pipeline.getSteps().size());
        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                1, pipeline.getConnections().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedStep() throws Exception {
        eventBus.post(new StepAddedEvent(new Step(eventBus, additionOperation)));
        serializeAndDeserialize();

        InputSocket<Double> a = (InputSocket<Double>) pipeline.getSteps().get(0).getInputSockets()[0];
        InputSocket<Double> b = (InputSocket<Double>) pipeline.getSteps().get(0).getInputSockets()[1];
        OutputSocket<Double> sum = (OutputSocket<Double>) pipeline.getSteps().get(0).getOutputSockets()[0];
        a.setValue(123.4);
        b.setValue(567.8);

        assertEquals((Double) (123.4 + 567.8), sum.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPythonStepFromURL() throws Exception {
        eventBus.post(new StepAddedEvent(new Step(eventBus, pythonAdditionOperationFromURL)));
        serializeAndDeserialize();

        InputSocket<Integer> a = (InputSocket<Integer>) pipeline.getSteps().get(0).getInputSockets()[0];
        InputSocket<Integer> b = (InputSocket<Integer>) pipeline.getSteps().get(0).getInputSockets()[1];
        OutputSocket<Integer> sum = (OutputSocket<Integer>) pipeline.getSteps().get(0).getOutputSockets()[0];
        a.setValue(1234);
        b.setValue(5678);

        assertEquals((Integer) (1234 + 5678), sum.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPythonStepFromSource() throws Exception {
        eventBus.post(new StepAddedEvent(new Step(eventBus, pythonAdditionOperationFromSource)));
        serializeAndDeserialize();

        InputSocket<Integer> a = (InputSocket<Integer>) pipeline.getSteps().get(0).getInputSockets()[0];
        InputSocket<Integer> b = (InputSocket<Integer>) pipeline.getSteps().get(0).getInputSockets()[1];
        OutputSocket<Integer> sum = (OutputSocket<Integer>) pipeline.getSteps().get(0).getOutputSockets()[0];
        a.setValue(1234);
        b.setValue(5678);

        assertEquals((Integer) (1234 + 5678), sum.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPipeline() throws Exception {
        Step step1 = new Step(eventBus, pythonAdditionOperationFromURL);
        Step step2 = new Step(eventBus, pythonAdditionOperationFromSource);
        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new ConnectionAddedEvent(new Connection(eventBus, step1.getOutputSockets()[0],
                step2.getInputSockets()[0])));
        serializeAndDeserialize();

        InputSocket<Integer> a1 = (InputSocket<Integer>) pipeline.getSteps().get(0).getInputSockets()[0];
        InputSocket<Integer> b1 = (InputSocket<Integer>) pipeline.getSteps().get(0).getInputSockets()[1];
        InputSocket<Integer> b2 = (InputSocket<Integer>) pipeline.getSteps().get(1).getInputSockets()[1];
        OutputSocket<Integer> sum2 = (OutputSocket<Integer>) pipeline.getSteps().get(1).getOutputSockets()[0];
        a1.setValue(123);
        b1.setValue(456);
        b2.setValue(789);

        assertEquals((Integer) (123 + 456 + 789), sum2.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPipelineWithMats() throws Exception {
        eventBus.post(new StepAddedEvent(new Step(eventBus, opencvAddOperation)));
        serializeAndDeserialize();

        Step step1 = pipeline.getSteps().get(0);
        InputSocket<Mat> a = (InputSocket<Mat>) step1.getInputSockets()[0];
        InputSocket<Mat> b = (InputSocket<Mat>) step1.getInputSockets()[1];
        OutputSocket<Mat> sum = (OutputSocket<Mat>) step1.getOutputSockets()[0];

        a.setValue(new Mat(1, 1, CV_32F, new Scalar(1234.5)));
        b.setValue(new Mat(1, 1, CV_32F, new Scalar(6789.0)));

        Mat diff = new Mat();
        Mat expected = new Mat(1, 1, CV_32F, new Scalar(1234.5 + 6789.0));
        compare(expected, sum.getValue(), diff, CMP_NE);
        assertEquals("Deserialized pipeline with Mat operations did not produce the expected sum.",
                0, countNonZero(diff));
    }
}
