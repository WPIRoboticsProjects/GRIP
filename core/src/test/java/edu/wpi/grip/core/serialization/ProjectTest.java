package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.events.StepAddedEvent;
import edu.wpi.grip.core.operations.PythonScriptOperation;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.util.Files;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static junit.framework.TestCase.assertEquals;
import static org.bytedeco.javacpp.opencv_core.*;

public class ProjectTest {

    private Injector injector;

    private Connection.Factory<Number> connectionFactory;
    private ImageFileSource.Factory imageSourceFactory;
    private Step.Factory stepFactory;
    private EventBus eventBus;
    private Pipeline pipeline;
    private Project project;

    private Operation additionOperation, opencvAddOperation, pythonAdditionOperationFromURL,
            pythonAdditionOperationFromSource;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new GRIPCoreModule());
        connectionFactory = injector
                .getInstance(Key.get(new TypeLiteral<Connection.Factory<Number>>() {}));
        imageSourceFactory = injector
                .getInstance(ImageFileSource.Factory.class);
        eventBus = injector.getInstance(EventBus.class);
        pipeline = injector.getInstance(Pipeline.class);
        project = injector.getInstance(Project.class);
        stepFactory = injector.getInstance(Step.Factory.class);


        additionOperation = new AdditionOperation();
        pythonAdditionOperationFromURL = new PythonScriptOperation(
                ProjectTest.class.getResource("/edu/wpi/grip/scripts/addition.py"));
        pythonAdditionOperationFromSource = new PythonScriptOperation("import edu.wpi.grip.core as grip\nimport java" +
                ".lang.Integer\n\ninputs = [\n    grip.SocketHints.createNumberSocketHint(\"a\", 0.0),\n    grip.SocketHints.createNumberSocketHint(" +
                "\"b\", 0.0),\n]\n\noutputs = [\n    grip.SocketHints.Outputs.createNumberSocketHint(\"sum\", 0.0)," +
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
        eventBus.post(new StepAddedEvent(stepFactory.create(additionOperation)));
        eventBus.post(new StepAddedEvent(stepFactory.create(pythonAdditionOperationFromSource)));
        eventBus.post(new StepAddedEvent(stepFactory.create(pythonAdditionOperationFromURL)));

        serializeAndDeserialize();

        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                3, pipeline.getSteps().size());
        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                0, pipeline.getConnections().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerializePipelineWithStepsAndConnections() throws Exception {
        Step step1 = stepFactory.create(pythonAdditionOperationFromSource);
        InputSocket<Number> a1 = (InputSocket<Number>) step1.getInputSockets()[0];
        InputSocket<Number> b1 = (InputSocket<Number>) step1.getInputSockets()[1];
        OutputSocket<Number> sum1 = (OutputSocket<Number>) step1.getOutputSockets()[0];

        Step step2 = stepFactory.create(pythonAdditionOperationFromURL);
        InputSocket<Number> a2 = (InputSocket<Number>) step2.getInputSockets()[0];
        InputSocket<Number> b2 = (InputSocket<Number>) step2.getInputSockets()[1];
        OutputSocket<Number> sum2 = (OutputSocket<Number>) step2.getOutputSockets()[0];

        a1.setValue(12);
        b1.setValue(34);
        b2.setValue(56);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new ConnectionAddedEvent(connectionFactory.create(sum1, a2)));

        serializeAndDeserialize();

        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                2, pipeline.getSteps().size());
        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                1, pipeline.getConnections().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedStep() throws Exception {
        eventBus.post(new StepAddedEvent(stepFactory.create(additionOperation)));
        serializeAndDeserialize();

        InputSocket<Number> a = (InputSocket<Number>) pipeline.getSteps().get(0).getInputSockets()[0];
        InputSocket<Number> b = (InputSocket<Number>) pipeline.getSteps().get(0).getInputSockets()[1];
        OutputSocket<Number> sum = (OutputSocket<Number>) pipeline.getSteps().get(0).getOutputSockets()[0];
        a.setValue(123.4);
        b.setValue(567.8);

        assertEquals((Double) (123.4 + 567.8), sum.getValue().get().doubleValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPythonStepFromURL() throws Exception {
        eventBus.post(new StepAddedEvent(stepFactory.create(pythonAdditionOperationFromURL)));
        serializeAndDeserialize();

        InputSocket<Number> a = (InputSocket<Number>) pipeline.getSteps().get(0).getInputSockets()[0];
        InputSocket<Number> b = (InputSocket<Number>) pipeline.getSteps().get(0).getInputSockets()[1];
        OutputSocket<Number> sum = (OutputSocket<Number>) pipeline.getSteps().get(0).getOutputSockets()[0];
        a.setValue(1234);
        b.setValue(5678);

        assertEquals((int) (1234 + 5678), sum.getValue().get().intValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPythonStepFromSource() throws Exception {
        eventBus.post(new StepAddedEvent(stepFactory.create(pythonAdditionOperationFromSource)));
        serializeAndDeserialize();

        InputSocket<Number> a = (InputSocket<Number>) pipeline.getSteps().get(0).getInputSockets()[0];
        InputSocket<Number> b = (InputSocket<Number>) pipeline.getSteps().get(0).getInputSockets()[1];
        OutputSocket<Number> sum = (OutputSocket<Number>) pipeline.getSteps().get(0).getOutputSockets()[0];
        a.setValue(1234);
        b.setValue(5678);

        assertEquals((int) (1234 + 5678), sum.getValue().get().intValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPipeline() throws Exception {
        Step step1 = stepFactory.create(pythonAdditionOperationFromURL);
        Step step2 = stepFactory.create(pythonAdditionOperationFromSource);
        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new ConnectionAddedEvent(
                connectionFactory.create(
                        (OutputSocket) step1.getOutputSockets()[0],
                        (InputSocket) step2.getInputSockets()[0]
                )));
        serializeAndDeserialize();

        InputSocket<Number> a1 = (InputSocket<Number>) pipeline.getSteps().get(0).getInputSockets()[0];
        InputSocket<Number> b1 = (InputSocket<Number>) pipeline.getSteps().get(0).getInputSockets()[1];
        InputSocket<Number> b2 = (InputSocket<Number>) pipeline.getSteps().get(1).getInputSockets()[1];
        OutputSocket<Number> sum2 = (OutputSocket<Number>) pipeline.getSteps().get(1).getOutputSockets()[0];
        a1.setValue(123);
        b1.setValue(456);
        b2.setValue(789);

        assertEquals((int) (123 + 456 + 789), sum2.getValue().get().intValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPipelineWithMats() throws Exception {
        eventBus.post(new StepAddedEvent(stepFactory.create(opencvAddOperation)));
        serializeAndDeserialize();

        Step step1 = pipeline.getSteps().get(0);
        InputSocket<Mat> a = (InputSocket<Mat>) step1.getInputSockets()[0];
        InputSocket<Mat> b = (InputSocket<Mat>) step1.getInputSockets()[1];
        OutputSocket<Mat> sum = (OutputSocket<Mat>) step1.getOutputSockets()[0];

        a.setValue(new Mat(1, 1, CV_32F, new Scalar(1234.5)));
        b.setValue(new Mat(1, 1, CV_32F, new Scalar(6789.0)));

        Mat diff = new Mat();
        Mat expected = new Mat(1, 1, CV_32F, new Scalar(1234.5 + 6789.0));
        compare(expected, sum.getValue().get(), diff, CMP_NE);
        assertEquals("Deserialized pipeline with Mat operations did not produce the expected sum.",
                0, countNonZero(diff));
    }

    @Test
    public void testSerializePipelineWithSource() throws Exception {
        final ImageFileSource source = imageSourceFactory.create(Files.gompeiJpegFile.file);
        source.load();
        eventBus.post(new SourceAddedEvent(source));

        serializeAndDeserialize();

        final ImageFileSource sourceDeserialized = (ImageFileSource) pipeline.getSources().get(0);
        Files.gompeiJpegFile.assertSameImage((Mat) sourceDeserialized.createOutputSockets()[0].getValue().get());
    }

}
