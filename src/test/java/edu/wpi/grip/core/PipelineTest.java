package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.events.*;
import edu.wpi.grip.core.sources.ImageFileSource;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PipelineTest {
    EventBus eventBus = new EventBus();
    Operation addition = new AdditionOperation();

    @Test
    public void testAddSource() throws URISyntaxException {
        Pipeline pipeline = new Pipeline(eventBus);
        Source source = new ImageFileSource(eventBus, new File(getClass().getResource("/edu/wpi/grip/images/GRIP_Logo.png").toURI()));

        eventBus.post(new SourceAddedEvent(source));

        assertEquals(Collections.singletonList(source), pipeline.getSources());
    }

    @Test
    public void testRemoveSource() throws URISyntaxException {
        Pipeline pipeline = new Pipeline(eventBus);
        Source source = new ImageFileSource(eventBus, new File(getClass().getResource("/edu/wpi/grip/images/GRIP_Logo.png").toURI()));

        eventBus.post(new SourceAddedEvent(source));
        eventBus.post(new SourceRemovedEvent(source));

        assertEquals(Collections.emptyList(), pipeline.getSources());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddStep() {
        Pipeline pipeline = new Pipeline(eventBus);
        Step step = new Step(eventBus, addition);

        eventBus.post(new StepAddedEvent(step));

        assertEquals(Collections.singletonList(step), pipeline.getSteps());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddStepAtIndex() {
        Pipeline pipeline = new Pipeline(eventBus);
        Step step1 = new Step(eventBus, addition);
        Step step2 = new Step(eventBus, addition);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2, 0));

        assertEquals(Arrays.asList(step2, step1), pipeline.getSteps());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveFirstStep() {
        Pipeline pipeline = new Pipeline(eventBus);
        Step step1 = new Step(eventBus, addition);
        Step step2 = new Step(eventBus, addition);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new StepRemovedEvent(step1));

        assertEquals(Collections.singletonList(step2), pipeline.getSteps());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveSecondStep() {
        Pipeline pipeline = new Pipeline(eventBus);
        Step step1 = new Step(eventBus, addition);
        Step step2 = new Step(eventBus, addition);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new StepRemovedEvent(step2));

        assertEquals(Collections.singletonList(step1), pipeline.getSteps());
    }

    @Test
    public void testMoveStep() {
        Pipeline pipeline = new Pipeline(eventBus);
        Step step1 = new Step(eventBus, addition);
        Step step2 = new Step(eventBus, addition);
        Step step3 = new Step(eventBus, addition);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new StepAddedEvent(step3));
        eventBus.post(new StepMovedEvent(step1, +1));

        assertEquals(Arrays.asList(step2, step1, step3), pipeline.getSteps());
    }

    @Test
    public void testMoveStepToBeginning() {
        Pipeline pipeline = new Pipeline(eventBus);
        Step step1 = new Step(eventBus, addition);
        Step step2 = new Step(eventBus, addition);
        Step step3 = new Step(eventBus, addition);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new StepAddedEvent(step3));
        eventBus.post(new StepMovedEvent(step2, -10));

        assertEquals(Arrays.asList(step2, step1, step3), pipeline.getSteps());
    }

    @Test
    public void testMoveStepToEnd() {
        Pipeline pipeline = new Pipeline(eventBus);
        Step step1 = new Step(eventBus, addition);
        Step step2 = new Step(eventBus, addition);
        Step step3 = new Step(eventBus, addition);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new StepAddedEvent(step3));
        eventBus.post(new StepMovedEvent(step2, +10));

        assertEquals(Arrays.asList(step1, step3, step2), pipeline.getSteps());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddConnection() throws Connection.InfiniteLoopException {
        Pipeline pipeline = new Pipeline(eventBus);
        Step step1 = new Step(eventBus, addition);
        Step step2 = new Step(eventBus, addition);
        Connection connection = new Connection(eventBus, step1.getOutputSockets()[0], step2.getInputSockets()[0]);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new ConnectionAddedEvent(connection));

        assertEquals(Collections.singleton(connection), pipeline.getConnections());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveConnection() throws Connection.InfiniteLoopException {
        Pipeline pipeline = new Pipeline(eventBus);
        Step step1 = new Step(eventBus, addition);
        Step step2 = new Step(eventBus, addition);
        Connection connection = new Connection(eventBus, step1.getOutputSockets()[0], step2.getInputSockets()[0]);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new ConnectionAddedEvent(connection));
        eventBus.post(new ConnectionRemovedEvent(connection));

        assertTrue(pipeline.getConnections().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPipeline() throws Connection.InfiniteLoopException {
        Pipeline pipeline = new Pipeline(eventBus);
        Step step1 = new Step(eventBus, addition);
        Step step2 = new Step(eventBus, addition);
        InputSocket<Double> a1 = (InputSocket<Double>) step1.getInputSockets()[0];
        InputSocket<Double> b1 = (InputSocket<Double>) step1.getInputSockets()[1];
        OutputSocket<Double> sum1 = (OutputSocket<Double>) step1.getOutputSockets()[0];
        InputSocket<Double> a2 = (InputSocket<Double>) step2.getInputSockets()[0];
        InputSocket<Double> b2 = (InputSocket<Double>) step2.getInputSockets()[1];
        OutputSocket<Double> sum2 = (OutputSocket<Double>) step2.getOutputSockets()[0];
        Connection connection = new Connection(eventBus, sum1, a2);

        // The result of this is the following equalities:
        //      sum1 = a1+b1
        //      sum2 = a2+b2
        //      a2 = sum1
        // So, sum2 will be equal to a1+b1+b2
        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new ConnectionAddedEvent(connection));

        a1.setValue(123.0);
        b1.setValue(456.0);
        b2.setValue(789.0);

        assertEquals((Double) 1368.0, sum2.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPipelineRemoved() throws Connection.InfiniteLoopException {
        Pipeline pipeline = new Pipeline(eventBus);
        Step step1 = new Step(eventBus, addition);
        Step step2 = new Step(eventBus, addition);
        InputSocket<Double> a1 = (InputSocket<Double>) step1.getInputSockets()[0];
        InputSocket<Double> b1 = (InputSocket<Double>) step1.getInputSockets()[1];
        OutputSocket<Double> sum1 = (OutputSocket<Double>) step1.getOutputSockets()[0];
        InputSocket<Double> a2 = (InputSocket<Double>) step2.getInputSockets()[0];
        InputSocket<Double> b2 = (InputSocket<Double>) step2.getInputSockets()[1];
        OutputSocket<Double> sum2 = (OutputSocket<Double>) step2.getOutputSockets()[0];
        Connection connection = new Connection(eventBus, sum1, a2);

        a2.setValue(0.0);

        eventBus.post(new StepAddedEvent(step1));
        eventBus.post(new StepAddedEvent(step2));
        eventBus.post(new ConnectionAddedEvent(connection));
        eventBus.post(new ConnectionRemovedEvent(connection));

        // Since the connection between sum1 and a2 was removed, a2 should still be equal to 0.0 here.
        a1.setValue(123.0);
        b1.setValue(456.0);
        b2.setValue(789.0);

        assertEquals((Double) 789.0, sum2.getValue());
    }
}
