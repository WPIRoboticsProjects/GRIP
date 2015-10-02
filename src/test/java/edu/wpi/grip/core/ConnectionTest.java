package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectionTest {
    private final EventBus eventBus = new EventBus();
    private final SocketHint<Double> fooHint = new SocketHint<>("foo", Double.class, 0.0);
    private final SocketHint<Double> barHint = new SocketHint<>("bar", Double.class, 0.0);
    private final Double testValue = 12345.6789;
    private OutputSocket<Double> foo;
    private InputSocket<Double> bar;

    @Before
    public void setUp(){
        foo = new OutputSocket<>(eventBus, fooHint);
        bar = new InputSocket<>(eventBus, barHint);
    }

    @Test
    public void testInputSocketChanges() {
        final Connection<Double> connection = new Connection<>(eventBus, foo, bar);

        foo.setValue(testValue);
        assertEquals(testValue, bar.getValue());

        eventBus.unregister(connection);
    }

    @Test(expected = NullPointerException.class)
    public void testEventBusNotNull() {
        Connection<Double> connection = new Connection<>(null, foo, bar);
    }

    @Test(expected = NullPointerException.class)
    public void testOutputSocketNotNull() {
        Connection<Double> connection = new Connection<>(eventBus, null, bar);
    }

    @Test(expected = NullPointerException.class)
    public void testInputSocketNotNull() {
        Connection<Double> connection = new Connection<>(eventBus, foo, null);
    }

}
