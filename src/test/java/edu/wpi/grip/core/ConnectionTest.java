package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectionTest {
    EventBus eventBus = new EventBus();
    SocketHint<Double> fooHint = new SocketHint<>("foo", Double.class, 0.0);
    SocketHint<Double> barHint = new SocketHint<>("bar", Double.class, 0.0);
    Double testValue = 12345.6789;

    @Test
    public void testInputSocketChanges() {
        Socket<Double> foo = new Socket<>(eventBus, fooHint);
        foo.setDirection(Socket.Direction.OUTPUT);

        Socket<Double> bar = new Socket<>(eventBus, barHint);
        bar.setDirection(Socket.Direction.INPUT);

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

    @Test(expected = IllegalArgumentException.class)
    public void testBackwardsConnection() {
        Socket<Double> output = new Socket<>(eventBus, fooHint);
        output.setDirection(Socket.Direction.OUTPUT);

        Socket<Double> input= new Socket<>(eventBus, fooHint);
        input.setDirection(Socket.Direction.INPUT);

        new Connection<Double>(eventBus, input, output);
    }
}
