package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.operations.PythonScriptFile;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.util.Files;
import edu.wpi.grip.util.GRIPCoreTestModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static org.bytedeco.javacpp.opencv_core.*;

public class ProjectTest {

    private GRIPCoreTestModule testModule;
    private Connection.Factory<Object> connectionFactory;
    private ImageFileSource.Factory imageSourceFactory;
    private Step.Factory stepFactory;
    private EventBus eventBus;
    private Pipeline pipeline;
    private Project project;
    private ManualPipelineRunner pipelineRunner;

    private OperationMetaData pythonAdditionOperationFromURL, pythonAdditionOperationFromSource;
    private OperationMetaData additionOperation, opencvAddOperation;

    @Before
    public void setUp() throws Exception {
        testModule = new GRIPCoreTestModule();
        testModule.setUp();
        final Injector injector = Guice.createInjector(testModule);
        connectionFactory = injector
                .getInstance(Key.get(new TypeLiteral<Connection.Factory<Object>>() {
                }));
        imageSourceFactory = injector
                .getInstance(ImageFileSource.Factory.class);
        eventBus = injector.getInstance(EventBus.class);
        project = injector.getInstance(Project.class);
        stepFactory = injector.getInstance(Step.Factory.class);
        final InputSocket.Factory isf = injector.getInstance(InputSocket.Factory.class);
        final OutputSocket.Factory osf = injector.getInstance(OutputSocket.Factory.class);

        pipeline = injector.getInstance(Pipeline.class);

        pipelineRunner = new ManualPipelineRunner(eventBus, pipeline);


        additionOperation = new OperationMetaData(AdditionOperation.DESCRIPTION, () -> new AdditionOperation(isf, osf));
        pythonAdditionOperationFromURL = PythonScriptFile.create(
                ProjectTest.class.getResource("/edu/wpi/grip/scripts/addition.py")).toOperationMetaData(isf, osf);

        pythonAdditionOperationFromSource = PythonScriptFile.create("import edu.wpi.grip.core.sockets as grip\nimport java" +
                ".lang.Integer\n\nname = \"Addition Operation\"\n\ninputs = [\n    grip.SocketHints.createNumberSocketHint(\"a\", 0.0),\n    grip.SocketHints.createNumberSocketHint(" +
                "\"b\", 0.0),\n]\n\noutputs = [\n    grip.SocketHints.Outputs.createNumberSocketHint(\"sum\", 0.0)," +
                "\n]\n\ndef perform(a, b):\n    return a + b\n").toOperationMetaData(isf, osf);
        opencvAddOperation = new OperationMetaData(AddOperation.DESCRIPTION, () -> new AddOperation(isf, osf));

        eventBus.post(new OperationAddedEvent(pythonAdditionOperationFromURL));
        eventBus.post(new OperationAddedEvent(pythonAdditionOperationFromSource));
        eventBus.post(new OperationAddedEvent(additionOperation));
        eventBus.post(new OperationAddedEvent(opencvAddOperation));
    }

    @After
    public void tearDown() {
        testModule.tearDown();
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
        pipeline.addStep(stepFactory.create(additionOperation));
        pipeline.addStep(stepFactory.create(pythonAdditionOperationFromSource));
        pipeline.addStep(stepFactory.create(pythonAdditionOperationFromURL));

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
        InputSocket<Number> a1 = (InputSocket<Number>) step1.getInputSockets().get(0);
        InputSocket<Number> b1 = (InputSocket<Number>) step1.getInputSockets().get(1);
        OutputSocket<Number> sum1 = (OutputSocket<Number>) step1.getOutputSockets().get(0);

        Step step2 = stepFactory.create(pythonAdditionOperationFromURL);
        InputSocket<Number> a2 = (InputSocket<Number>) step2.getInputSockets().get(0);
        InputSocket<Number> b2 = (InputSocket<Number>) step2.getInputSockets().get(1);
        OutputSocket<Number> sum2 = (OutputSocket<Number>) step2.getOutputSockets().get(0);

        a1.setValue(12);
        b1.setValue(34);
        b2.setValue(56);

        pipeline.addStep(step1);
        pipeline.addStep(step2);
        eventBus.post(new ConnectionAddedEvent(connectionFactory.create(sum1, (InputSocket) a2)));

        serializeAndDeserialize();

        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                2, pipeline.getSteps().size());

        assertEquals("Serialized pipeline is not equal to pipeline before serialization",
                1, pipeline.getConnections().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedStep() throws Exception {
        pipeline.addStep(stepFactory.create(additionOperation));
        serializeAndDeserialize();


        final Step fromPipeline = pipeline.getSteps().get(0);
        InputSocket<Number> a = (InputSocket<Number>) fromPipeline.getInputSockets().get(0);
        InputSocket<Number> b = (InputSocket<Number>) fromPipeline.getInputSockets().get(1);
        OutputSocket<Number> sum = (OutputSocket<Number>) fromPipeline.getOutputSockets().get(0);

        a.setValue(123.4);
        b.setValue(567.8);

        pipelineRunner.runPipeline();

        assertEquals((Double) (123.4 + 567.8), sum.getValue().get().doubleValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPythonStepFromURL() throws Exception {
        pipeline.addStep(stepFactory.create(pythonAdditionOperationFromURL));
        serializeAndDeserialize();

        final Step fromPipeline = pipeline.getSteps().get(0);
        InputSocket<Number> a = (InputSocket<Number>) fromPipeline.getInputSockets().get(0);
        InputSocket<Number> b = (InputSocket<Number>) fromPipeline.getInputSockets().get(1);
        OutputSocket<Number> sum = (OutputSocket<Number>) fromPipeline.getOutputSockets().get(0);


        a.setValue(1234);
        b.setValue(5678);

        pipelineRunner.runPipeline();

        assertEquals((int) (1234 + 5678), sum.getValue().get().intValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPythonStepFromSource() throws Exception {
        pipeline.addStep(stepFactory.create(pythonAdditionOperationFromSource));
        serializeAndDeserialize();

        final Step fromPipeline = pipeline.getSteps().get(0);
        InputSocket<Number> a = (InputSocket<Number>) fromPipeline.getInputSockets().get(0);
        InputSocket<Number> b = (InputSocket<Number>) fromPipeline.getInputSockets().get(1);
        OutputSocket<Number> sum = (OutputSocket<Number>) fromPipeline.getOutputSockets().get(0);


        a.setValue(1234);
        b.setValue(5678);

        pipelineRunner.runPipeline();

        assertEquals((int) (1234 + 5678), sum.getValue().get().intValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPipeline() throws Exception {
        Step step1 = stepFactory.create(pythonAdditionOperationFromURL);
        Step step2 = stepFactory.create(pythonAdditionOperationFromSource);
        pipeline.addStep(step1);
        pipeline.addStep(step2);
        eventBus.post(new ConnectionAddedEvent(
                connectionFactory.create(
                        (OutputSocket) step1.getOutputSockets().get(0),
                        (InputSocket) step2.getInputSockets().get(0)
                )));
        serializeAndDeserialize();

        final Step step1Out = pipeline.getSteps().get(0);
        final Step step2Out = pipeline.getSteps().get(1);

        InputSocket<Number> a1 = (InputSocket<Number>) step1Out.getInputSockets().get(0);
        InputSocket<Number> b1 = (InputSocket<Number>) step1Out.getInputSockets().get(1);
        InputSocket<Number> b2 = (InputSocket<Number>) step2Out.getInputSockets().get(1);
        OutputSocket<Number> sum2 = (OutputSocket<Number>) step2Out.getOutputSockets().get(0);


        a1.setValue(123);
        b1.setValue(456);
        b2.setValue(789);

        pipelineRunner.runPipeline();

        assertEquals((int) (123 + 456 + 789), sum2.getValue().get().intValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPerformSerializedPipelineWithMats() throws Exception {
        pipeline.addStep(stepFactory.create(opencvAddOperation));
        serializeAndDeserialize();

        Step step1 = pipeline.getSteps().get(0);
        InputSocket<Mat> a = (InputSocket<Mat>) step1.getInputSockets().get(0);
        InputSocket<Mat> b = (InputSocket<Mat>) step1.getInputSockets().get(1);
        OutputSocket<Mat> sum = (OutputSocket<Mat>) step1.getOutputSockets().get(0);


        a.setValue(new Mat(1, 1, CV_32F, new Scalar(1234.5)));
        b.setValue(new Mat(1, 1, CV_32F, new Scalar(6789.0)));

        pipelineRunner.runPipeline();

        Mat diff = new Mat();
        Mat expected = new Mat(1, 1, CV_32F, new Scalar(1234.5 + 6789.0));
        compare(expected, sum.getValue().get(), diff, CMP_NE);
        assertEquals("Deserialized pipeline with Mat operations did not produce the expected sum.",
                0, countNonZero(diff));
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void testSerializePipelineWithSource() throws Exception {
        final ImageFileSource source = imageSourceFactory.create(Files.gompeiJpegFile.file);
        source.initialize();
        eventBus.post(new SourceAddedEvent(source));

        serializeAndDeserialize();

        final ImageFileSource sourceDeserialized = (ImageFileSource) pipeline.getSources().get(0);
        Files.gompeiJpegFile.assertSameImage((Mat) sourceDeserialized.createOutputSockets().get(0).getValue().get());
    }

    @Test
    public void testSerializedProjectSettings() {
        ProjectSettings projectSettings = new ProjectSettings();
        projectSettings.setTeamNumber(190);
        projectSettings.setDeployAddress("roborio-191-frc.local");
        eventBus.post(new ProjectSettingsChangedEvent(projectSettings));

        serializeAndDeserialize();

        assertEquals("Team number was not serialized/deserialized",
                190, pipeline.getProjectSettings().getTeamNumber());
        assertEquals("Deploy address was not serialized/deserialized",
                "roborio-191-frc.local", pipeline.getProjectSettings().getDeployAddress());
    }

    @Test
    public void testUnspecifiedProjectSettings() {
        Reader reader = new StringReader("<grip:Pipeline>" +
                "  <sources/>" +
                "  <steps/>" +
                "  <connections/>" +
                "  <settings/>" +
                "</grip:Pipeline>");

        project.open(reader);

        assertNotNull("Project setting was null", pipeline.getProjectSettings().getDeployJvmOptions());
    }
}
