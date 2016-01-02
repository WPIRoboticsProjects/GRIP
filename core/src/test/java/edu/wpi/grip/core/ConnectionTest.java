package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectionTest {
    private static final Number testValue = Double.valueOf(12345.6789);

    private EventBus eventBus;
    private SocketHint<Number> fooHint;
    private SocketHint<Number> barHint;

    private OutputSocket<Number> foo;
    private InputSocket<Number> bar;

    private class MockPipeline extends Pipeline {
    }

    @Before
    public void setUp() {
        eventBus = new EventBus();

        fooHint = SocketHints.createNumberSocketHint("foo", 0.0);
        barHint = SocketHints.createNumberSocketHint("bar", 0.0);


        foo = new OutputSocket<>(eventBus, fooHint);
        eventBus.register(foo);
        bar = new InputSocket<>(eventBus, barHint);
        eventBus.register(bar);
    }

    @Test
    public void testInputSocketChanges() {
        final Connection<Number> connection = new Connection(eventBus, new MockPipeline(), foo, bar);
        eventBus.register(connection);

        foo.setValue(testValue);
        assertEquals(testValue, bar.getValue().get());

        eventBus.unregister(connection);
    }

    @Test
    public void testInputSocketResets() {
        final Connection<Number> connection = new Connection<>(eventBus, new MockPipeline(), foo, bar);

        foo.setValue(testValue);
        eventBus.post(new ConnectionRemovedEvent(connection));
        assertEquals(0.0, bar.getValue().get().doubleValue(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPipelineSaysConnectionIsInvalid() {
        new Connection(
                new EventBus(),
                new Pipeline() {
                    @Override
                    public boolean canConnect(Socket socket1, Socket socket2) {
                        return false;
                    }
                },
                foo,
                bar
        );
    }


}
