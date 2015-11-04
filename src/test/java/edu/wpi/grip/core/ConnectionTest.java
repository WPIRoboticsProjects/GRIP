package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ConnectionTest {
    private final EventBus eventBus = new EventBus();
    private final SocketHint<Double> fooHint = new SocketHint<>("foo", Double.class, 0.0);
    private final SocketHint<Double> barHint = new SocketHint<>("bar", Double.class, 0.0);
    private final Double testValue = 12345.6789;
    private OutputSocket<Double> foo;
    private InputSocket<Double> bar;

    @Before
    public void setUp() {
        foo = new OutputSocket<>(eventBus, fooHint);
        bar = new InputSocket<>(eventBus, barHint);
    }

    @Test
    public void testInputSocketChanges() throws Connection.InfiniteLoopException {
        final Connection<Double> connection = new Connection<>(eventBus, foo, bar);

        foo.setValue(testValue);
        assertEquals(testValue, bar.getValue());

        eventBus.unregister(connection);
    }

    @Test(expected = NullPointerException.class)
    public void testEventBusNotNull() throws Connection.InfiniteLoopException {
        Connection<Double> connection = new Connection<>(null, foo, bar);
    }

    @Test(expected = NullPointerException.class)
    public void testOutputSocketNotNull() throws Connection.InfiniteLoopException {
        Connection<Double> connection = new Connection<>(eventBus, null, bar);
    }

    @Test(expected = NullPointerException.class)
    public void testInputSocketNotNull() throws Connection.InfiniteLoopException {
        Connection<Double> connection = new Connection<>(eventBus, foo, null);
    }

    @Test(expected = Connection.InfiniteLoopException.class)
    public void testNoInfiniteLoopSameStep() throws Connection.InfiniteLoopException {
        final Step testStep = new Step(eventBus, new AdditionOperation());
        new Connection(eventBus, testStep.getOutputSockets()[0], testStep.getInputSockets()[0]);
    }

    @Test(expected = Connection.InfiniteLoopException.class)
    public void testNoInfiniteLoopDifferentStep() throws Connection.InfiniteLoopException {
        final Step testStep = new Step(eventBus, new AdditionOperation());
        final Step testStep2 = new Step(eventBus, new AdditionOperation());

        try {
            final Connection testStepToTestStep2Connection = new Connection(eventBus, testStep.getOutputSockets()[0], testStep2.getInputSockets()[0]);
            eventBus.post(new ConnectionAddedEvent(testStepToTestStep2Connection));
        } catch (Connection.InfiniteLoopException e){
            fail("Should not have thrown an infinite loop exception while connecting testStep and testStep2");
        }

        new Connection(eventBus, testStep2.getOutputSockets()[0], testStep.getInputSockets()[0]);
    }

}
