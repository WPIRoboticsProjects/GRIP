package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.sockets.*;
import edu.wpi.grip.util.GRIPCoreTestModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class PipelineTest {

    private GRIPCoreTestModule testModule;
    private Step.Factory stepFactory;
    private EventBus eventBus;
    private Pipeline pipeline;
    private OperationMetaData additionMeta;
    private InputSocket.Factory isf;
    private OutputSocket.Factory osf;

    private class MockConnection extends Connection {

        /**
         * @param eventBus
         * @param pipeline The pipeline to create the connection inside of.
         */
        public MockConnection(EventBus eventBus, Pipeline pipeline) {
            super(eventBus, pipeline, new MockOutputSocket("Whatever output"), new MockInputSocket("Whatever input"));
        }
    }

    @Before
    public void setUp() {
        testModule = new GRIPCoreTestModule();
        testModule.setUp();
        final Injector injector = Guice.createInjector(testModule);
        stepFactory = injector.getInstance(Step.Factory.class);
        eventBus = injector.getInstance(EventBus.class);
        pipeline = injector.getInstance(Pipeline.class);
        isf = injector.getInstance(InputSocket.Factory.class);
        osf = injector.getInstance(OutputSocket.Factory.class);
        additionMeta = new OperationMetaData(AdditionOperation.DESCRIPTION, () -> new AdditionOperation(isf, osf));
    }

    @After
    public void tearDown() {
        testModule.tearDown();
    }

    @Test
    public void testAddSource() throws URISyntaxException, IOException {
        Source source = new MockSource();

        eventBus.post(new SourceAddedEvent(source));

        assertEquals("The source was not added to the pipeline", Collections.singletonList(source), pipeline.getSources());
    }

    @Test
    public void testRemoveSource() throws URISyntaxException, IOException {
        Source source = new MockSource();

        eventBus.post(new SourceAddedEvent(source));
        eventBus.post(new SourceRemovedEvent(source));

        assertEquals("The source was not added then removed from the pipeline", Collections.emptyList(), pipeline.getSources());

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddStep() {
        Step step = new MockStep();

        pipeline.addStep(step);

        assertEquals("The step was not added to the pipeline", Collections.singletonList(step), pipeline.getSteps());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddStepAtIndex() {
        Step step1 = new MockStep();
        Step step2 = new MockStep();

        pipeline.addStep(step1);
        pipeline.addStep(0, step2);

        assertEquals("The steps were not added to the pipeline", Arrays.asList(step2, step1), pipeline.getSteps());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveFirstStep() {
        Step step1 = MockStep.createMockStepWithOperation();
        Step step2 = new MockStep();

        pipeline.addStep(step1);
        pipeline.addStep(step2);
        pipeline.removeStep(step1);

        assertEquals("There was not one step left after the first was removed", Collections.singletonList(step2), pipeline.getSteps());

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveSecondStep() {
        Step step1 = new MockStep();
        Step step2 = MockStep.createMockStepWithOperation();

        pipeline.addStep(step1);
        pipeline.addStep(step2);
        pipeline.removeStep(step2);

        assertEquals("There was not one step left after the second was removed", Collections.singletonList(step1), pipeline.getSteps());

    }

    @Test
    public void testMoveStep() {
        Step step1 = new MockStep();
        Step step2 = new MockStep();
        Step step3 = new MockStep();

        pipeline.addStep(step1);
        pipeline.addStep(step2);
        pipeline.addStep(step3);
        pipeline.moveStep(step1, +1);

        assertEquals("The step order did not change as expected", Arrays.asList(step2, step1, step3), pipeline.getSteps());

    }

    @Test
    public void testMoveStepToBeginning() {
        Step step1 = new MockStep();
        Step step2 = new MockStep();
        Step step3 = new MockStep();

        pipeline.addStep(step1);
        pipeline.addStep(step2);
        pipeline.addStep(step3);
        pipeline.moveStep(step2, -10);
        assertEquals("The step was not moved to the beginning of the pipeline", Arrays.asList(step2, step1, step3), pipeline.getSteps());
    }

    @Test
    public void testMoveStepToEnd() {
        Step step1 = new MockStep();
        Step step2 = new MockStep();
        Step step3 = new MockStep();

        pipeline.addStep(step1);
        pipeline.addStep(step2);
        pipeline.addStep(step3);
        pipeline.moveStep(step2, +10);

        assertEquals("The step was not moved to the end of the pipeline", Arrays.asList(step1, step3, step2), pipeline.getSteps());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddConnection() {
        Connection connection = new MockConnection(eventBus, pipeline);
        eventBus.post(new ConnectionAddedEvent(connection));

        assertEquals("The connection was not added to the pipeline", Collections.singleton(connection), pipeline.getConnections());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveConnection() {
        Connection connection = new MockConnection(eventBus, pipeline);
        eventBus.post(new ConnectionAddedEvent(connection));
        eventBus.post(new ConnectionRemovedEvent(connection));

        assertTrue("The pipeline was not empty", pipeline.getConnections().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPipeline() {
        Step step1 = stepFactory.create(new OperationMetaData(AdditionOperation.DESCRIPTION, () -> new AdditionOperation(isf, osf)));
        Step step2 = stepFactory.create(new OperationMetaData(AdditionOperation.DESCRIPTION, () -> new AdditionOperation(isf, osf)));

        InputSocket<Double> a1 = (InputSocket<Double>) step1.getInputSockets().get(0);
        InputSocket<Double> b1 = (InputSocket<Double>) step1.getInputSockets().get(1);
        OutputSocket<Double> sum1 = (OutputSocket<Double>) step1.getOutputSockets().get(0);
        InputSocket<Double> a2 = (InputSocket<Double>) step2.getInputSockets().get(0);
        InputSocket<Double> b2 = (InputSocket<Double>) step2.getInputSockets().get(1);
        OutputSocket<Double> sum2 = (OutputSocket<Double>) step2.getOutputSockets().get(0);

        // The result of this is the following equalities:
        //      sum1 = a1+b1
        //      sum2 = a2+b2
        //      a2 = sum1
        // So, sum2 will be equal to a1+b1+b2
        pipeline.addStep(step1);
        pipeline.addStep(step2);

        Connection connection = new Connection(eventBus, pipeline, sum1, a2);
        eventBus.register(connection);
        eventBus.post(new ConnectionAddedEvent(connection));

        ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);

        a1.setValue(123.0);
        b1.setValue(456.0);
        b2.setValue(789.0);

        runner.runPipeline();

        assertEquals((Double) 1368.0, sum2.getValue().get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPipelineRemoved() {
        Step step1 = stepFactory.create(new OperationMetaData(AdditionOperation.DESCRIPTION, () -> new AdditionOperation(isf, osf)));
        Step step2 = stepFactory.create(new OperationMetaData(AdditionOperation.DESCRIPTION, () -> new AdditionOperation(isf, osf)));

        InputSocket<Double> a1 = (InputSocket<Double>) step1.getInputSockets().get(0);
        InputSocket<Double> b1 = (InputSocket<Double>) step1.getInputSockets().get(1);
        OutputSocket<Double> sum1 = (OutputSocket<Double>) step1.getOutputSockets().get(0);
        InputSocket<Double> a2 = (InputSocket<Double>) step2.getInputSockets().get(0);
        InputSocket<Double> b2 = (InputSocket<Double>) step2.getInputSockets().get(1);
        OutputSocket<Double> sum2 = (OutputSocket<Double>) step2.getOutputSockets().get(0);

        a2.setValue(0.0);

        pipeline.addStep(step1);
        pipeline.addStep(step2);

        Connection connection = new Connection(eventBus, pipeline, sum1, a2);
        eventBus.post(new ConnectionAddedEvent(connection));
        eventBus.post(new ConnectionRemovedEvent(connection));

        ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);

        // Since the connection between sum1 and a2 was removed, a2 should still be equal to 0.0 here.
        a1.setValue(123.0);
        b1.setValue(456.0);
        b2.setValue(789.0);

        runner.runPipeline();

        assertEquals((Double) 789.0, sum2.getValue().get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCannotConnectBackwards() {
        Step step1 = stepFactory.create(additionMeta);
        Step step2 = stepFactory.create(additionMeta);
        InputSocket<Double> a1 = (InputSocket<Double>) step1.getInputSockets().get(0);
        OutputSocket<Double> sum2 = (OutputSocket<Double>) step2.getOutputSockets().get(0);

        pipeline.addStep(step1);
        pipeline.addStep(step2);
        assertFalse("Should not be able to connect backwards", pipeline.canConnect(a1, sum2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCannotConnectIncompatibleTypes() {
        InputSocket<Number> a = isf.create(SocketHints.createNumberSocketHint("a", 0.0));
        OutputSocket<String> b = osf.create(new SocketHint.Builder<>(String.class).identifier("b").initialValue("").build());

        assertFalse("Should not be able to connect incompatible types", pipeline.canConnect((OutputSocket) b, (InputSocket) a));
    }

    @Test
    public void testAddBetweenSteps() {
        final Step
                stepToAdd = new MockStep(),
                lowerStep = new MockStep(),
                upperStep = new MockStep();
        pipeline.addStep(lowerStep);
        pipeline.addStep(upperStep);

        pipeline.addStepBetween(stepToAdd, lowerStep, upperStep);
        assertEquals("The step was not added to the middle of the pipeline",
                Arrays.asList(lowerStep, stepToAdd, upperStep), pipeline.getSteps());
    }

    @Test
    public void testAddBetweenNullAndStep() {
        final Step
                stepToAdd = new MockStep(),
                lowerStep = new MockStep(),
                upperStep = new MockStep();
        pipeline.addStep(lowerStep);
        pipeline.addStep(upperStep);
        pipeline.addStepBetween(stepToAdd, null, lowerStep);
        assertEquals("The step was not added to the begining of the pipeline",
                Arrays.asList(stepToAdd, lowerStep, upperStep), pipeline.getSteps());
    }

    @Test
    public void testAddBetweenStepAndNull() {
        final Step
                stepToAdd = new MockStep(),
                lowerStep = new MockStep(),
                upperStep = new MockStep();
        pipeline.addStep(lowerStep);
        pipeline.addStep(upperStep);
        pipeline.addStepBetween(stepToAdd, upperStep, null);
        assertEquals("The step was not added to the end of the pipeline",
                Arrays.asList(lowerStep, upperStep, stepToAdd), pipeline.getSteps());
    }

    @Test
    public void testAddBetweenTwoNulls() {
        final Step stepToAdd = new MockStep();
        pipeline.addStepBetween(stepToAdd, null, null);
        assertEquals("The step should have been added to the pipeline",
                Collections.singletonList(stepToAdd), pipeline.getSteps());
    }

    @Test(expected = AssertionError.class)
    public void testAddBetweenStepsOutOfOrder() {
        final Step
                stepToAdd = new MockStep(),
                lowerStep = new MockStep(),
                upperStep = new MockStep();
        pipeline.addStep(lowerStep);
        pipeline.addStep(upperStep);

        pipeline.addStepBetween(stepToAdd, upperStep, lowerStep);
    }

    @Test
    public void testMoveStepToLeft() {
        final Step
                stepToMove = new MockStep(),
                lowerStep = new MockStep(),
                upperStep = new MockStep();
        pipeline.addStep(lowerStep);
        pipeline.addStep(upperStep);
        pipeline.addStep(stepToMove);
        pipeline.moveStepBetween(stepToMove, lowerStep, upperStep);

        assertEquals("The step should have been moved within the pipeline",
                Arrays.asList(lowerStep, stepToMove, upperStep), pipeline.getSteps());
    }

    @Test
    public void testMoveStepToRight() {
        final Step
                stepToMove = new MockStep(),
                lowerStep = new MockStep(),
                upperStep = new MockStep();
        pipeline.addStep(stepToMove);
        pipeline.addStep(lowerStep);
        pipeline.addStep(upperStep);
        pipeline.moveStepBetween(stepToMove, lowerStep, upperStep);

        assertEquals("The step should have been moved within the pipeline",
                Arrays.asList(lowerStep, stepToMove, upperStep), pipeline.getSteps());
    }
}
