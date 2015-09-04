package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectionTest {
    EventBus eventBus = new EventBus();
    SocketHint<Double> fooHint = new SocketHint<>("foo", Double.class, SocketHint.View.NONE, null, 0.0);
    SocketHint<Double> barHint = new SocketHint<>("bar", Double.class, SocketHint.View.NONE, null, 0.0);
    Double testValue = 12345.6789;

    @Test
    public void testInputSocketChanges() {
        Socket<Double> foo = new Socket<>(eventBus, fooHint);
        Socket<Double> bar = new Socket<>(eventBus, barHint);
        Connection<Double> connection = new Connection<>(eventBus, foo, bar);

        foo.setValue(testValue);
        assertEquals(testValue, bar.getValue());

        eventBus.unregister(connection);
    }

    @Test(expected = NullPointerException.class)
    public void testEventBusNotNull() {
        Socket<Double> foo = new Socket<>(eventBus, fooHint);
        Socket<Double> bar = new Socket<>(eventBus, barHint);
        Connection<Double> connection = new Connection<>(null, foo, bar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInputSocketNotEqualToOutputSocket() {
        Socket<Double> foo = new Socket<>(eventBus, fooHint);
        Connection<Double> connection = new Connection<>(eventBus, foo, foo);
    }

    @Test(expected = NullPointerException.class)
    public void testOutputSocketNotNull() {
        Socket<Double> foo = new Socket<>(eventBus, fooHint);
        Connection<Double> connection = new Connection<>(eventBus, null, foo);
    }

    @Test(expected = NullPointerException.class)
    public void testInputSocketNotNull() {
        Socket<Double> foo = new Socket<>(eventBus, fooHint);
        Connection<Double> connection = new Connection<>(eventBus, foo, null);
    }
}
