package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectionTest {
    private final EventBus eventBus = new EventBus();
    private final SocketHint<Number> fooHint = SocketHints.createNumberSocketHint("foo", 0.0);
    private final SocketHint<Number> barHint = SocketHints.createNumberSocketHint("bar", 0.0);
    private final Double testValue = 12345.6789;
    private OutputSocket<Number> foo;
    private InputSocket<Number> bar;

    @Before
    public void setUp() {
        foo = new OutputSocket<>(eventBus, fooHint);
        bar = new InputSocket<>(eventBus, barHint);
    }

    @Test
    public void testInputSocketChanges() {
        final Connection<Number> connection = new Connection<>(eventBus, foo, bar);

        foo.setValue(testValue);
        assertEquals(testValue, bar.getValue().get());

        eventBus.unregister(connection);
    }

    @Test
    public void testInputSocketResets() {
        final Connection<Number> connection = new Connection<>(eventBus, foo, bar);

        foo.setValue(testValue);
        eventBus.post(new ConnectionRemovedEvent(connection));
        assertEquals(0.0, bar.getValue().get().doubleValue(), 0.01);

        eventBus.unregister(connection);
    }

    @Test(expected = NullPointerException.class)
    public void testEventBusNotNull() {
        Connection<Number> connection = new Connection<>(null, foo, bar);
    }

    @Test(expected = NullPointerException.class)
    public void testOutputSocketNotNull() {
        Connection<Number> connection = new Connection<>(eventBus, null, bar);
    }

    @Test(expected = NullPointerException.class)
    public void testInputSocketNotNull() {
        Connection<Number> connection = new Connection<>(eventBus, foo, null);
    }

}
