package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.events.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.*;

public class PipelineTest {

    private Injector injector;
    private Step.Factory stepFactory;
    private EventBus eventBus;
    private Pipeline pipeline;
    private Operation addition;


    private class MockSource extends Source {
        @Override
        public String getName() {
            return null;
        }

        @Override
        protected OutputSocket[] createOutputSockets() {
            return new OutputSocket[0];
        }

        @Override
        public Properties getProperties() {
            return null;
        }
    }

    private class MockConnection extends Connection {

        /**
         * @param eventBus
         * @param pipeline     The pipeline to create the connection inside of.
         */
        public MockConnection(EventBus eventBus, Pipeline pipeline) {
            super(eventBus, pipeline, new MockOutputSocket("Whatever output"), new MockInputSocket("Whatever input"));
        }
    }

    @Before
    public void setUp () {
        injector = Guice.createInjector(new GRIPCoreModule());
        stepFactory = injector.getInstance(Step.Factory.class);
        eventBus = injector.getInstance(EventBus.class);
        pipeline = injector.getInstance(Pipeline.class);
        addition = new AdditionOperation();
    }

    @Test
    public void testAddSource() throws URISyntaxException, IOException {
        Source source = new MockSource();

        eventBus.post(new SourceAddedEvent(source));

        assertEquals(Collections.singletonList(source), pipeline.getSources());
    }

    @Test
    public void testRemoveSource() throws URISyntaxException, IOException {
        Source source = new MockSource();

        eventBus.post(new SourceAddedEvent(source));
        eventBus.post(new SourceRemovedEvent(source));

        assertEquals(Collections.emptyList(), pipeline.getSources());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddStep() {
        Step step = new MockStep();

        eventBus.post(new StepAddedEvent(step));

        assertEquals(Collections.singletonList(step), pipeline.getSteps());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddStepAtIndex() {
        Step step1 = new MockStep();
        Step step2 = new MockStep();

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2, 0));

        assertEquals(Arrays.asList(step2, step1), pipeline.getSteps());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveFirstStep() {
        Step step1 = new MockStep();
        Step step2 = new MockStep();

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new StepRemovedEvent(step1));

        assertEquals(Collections.singletonList(step2), pipeline.getSteps());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveSecondStep() {
        Step step1 = new MockStep();
        Step step2 = new MockStep();

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new StepRemovedEvent(step2));

        assertEquals(Collections.singletonList(step1), pipeline.getSteps());
    }

    @Test
    public void testMoveStep() {
        Step step1 = new MockStep();
        Step step2 = new MockStep();
        Step step3 = new MockStep();

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new StepAddedEvent(step3));
        eventBus.post(new StepMovedEvent(step1, +1));

        assertEquals(Arrays.asList(step2, step1, step3), pipeline.getSteps());
    }

    @Test
    public void testMoveStepToBeginning() {
        Step step1 = new MockStep();
        Step step2 = new MockStep();
        Step step3 = new MockStep();

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new StepAddedEvent(step3));
        eventBus.post(new StepMovedEvent(step2, -10));

        assertEquals("The step was not moved to the beginning of the pipeline", Arrays.asList(step2, step1, step3), pipeline.getSteps());
    }

    @Test
    public void testMoveStepToEnd() {
        Step step1 = new MockStep();
        Step step2 = new MockStep();
        Step step3 = new MockStep();

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new StepAddedEvent(step3));
        eventBus.post(new StepMovedEvent(step2, +10));

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
        Step step1 = stepFactory.create(addition);
        Step step2 = stepFactory.create(addition);
        InputSocket<Double> a1 = (InputSocket<Double>) step1.getInputSockets()[0];
        InputSocket<Double> b1 = (InputSocket<Double>) step1.getInputSockets()[1];
        OutputSocket<Double> sum1 = (OutputSocket<Double>) step1.getOutputSockets()[0];
        InputSocket<Double> a2 = (InputSocket<Double>) step2.getInputSockets()[0];
        InputSocket<Double> b2 = (InputSocket<Double>) step2.getInputSockets()[1];
        OutputSocket<Double> sum2 = (OutputSocket<Double>) step2.getOutputSockets()[0];

        // The result of this is the following equalities:
        //      sum1 = a1+b1
        //      sum2 = a2+b2
        //      a2 = sum1
        // So, sum2 will be equal to a1+b1+b2
        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));

        Connection connection = new Connection(eventBus, pipeline, sum1, a2);
        eventBus.register(connection);
        eventBus.post(new ConnectionAddedEvent(connection));

        a1.setValue(123.0);
        b1.setValue(456.0);
        b2.setValue(789.0);

        assertEquals((Double) 1368.0, sum2.getValue().get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPipelineRemoved() {
        Step step1 = stepFactory.create(addition);
        Step step2 = stepFactory.create(addition);
        InputSocket<Double> a1 = (InputSocket<Double>) step1.getInputSockets()[0];
        InputSocket<Double> b1 = (InputSocket<Double>) step1.getInputSockets()[1];
        OutputSocket<Double> sum1 = (OutputSocket<Double>) step1.getOutputSockets()[0];
        InputSocket<Double> a2 = (InputSocket<Double>) step2.getInputSockets()[0];
        InputSocket<Double> b2 = (InputSocket<Double>) step2.getInputSockets()[1];
        OutputSocket<Double> sum2 = (OutputSocket<Double>) step2.getOutputSockets()[0];

        a2.setValue(0.0);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));

        Connection connection = new Connection(eventBus, pipeline, sum1, a2);
        eventBus.post(new ConnectionAddedEvent(connection));
        eventBus.post(new ConnectionRemovedEvent(connection));

        // Since the connection between sum1 and a2 was removed, a2 should still be equal to 0.0 here.
        a1.setValue(123.0);
        b1.setValue(456.0);
        b2.setValue(789.0);

        assertEquals((Double) 789.0, sum2.getValue().get());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCannotConnectBackwards() {
        Step step1 = stepFactory.create(addition);
        Step step2 = stepFactory.create(addition);
        InputSocket<Double> a1 = (InputSocket<Double>) step1.getInputSockets()[0];
        OutputSocket<Double> sum2 = (OutputSocket<Double>) step2.getOutputSockets()[0];

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        assertFalse("Should not be able to connect backwards", pipeline.canConnect(a1, sum2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCannotConnectIncompatibleTypes() {
        InputSocket<Number> a = new InputSocket<>(eventBus, SocketHints.createNumberSocketHint("a", 0.0));
        OutputSocket<String> b = new OutputSocket<>(eventBus, new SocketHint.Builder<>(String.class).identifier("b").initialValue("").build());

        assertFalse("Should not be able to connect incompatible types", pipeline.canConnect((OutputSocket) b, (InputSocket) a));
    }
}
