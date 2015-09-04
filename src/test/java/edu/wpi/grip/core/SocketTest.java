package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.SocketChangedEvent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SocketTest {
    EventBus eventBus = new EventBus();
    Double testValue = 12345.6789;

    @Test
    public void testGetSocketHint() throws Exception {
        SocketHint<Double> socketHint = new SocketHint<>("foo", Double.class, SocketHint.View.SLIDER);
        Socket<Double> socket = new Socket<Double>(eventBus, socketHint);

        assertEquals("foo", socket.getSocketHint().getIdentifier());
        assertEquals(Double.class, socket.getSocketHint().getType());
        assertEquals(SocketHint.View.SLIDER, socket.getSocketHint().getView());
    }

    @Test
    public void testSetValue() throws Exception {
        SocketHint<Double> socketHint = new SocketHint<>("foo", Double.class, SocketHint.View.SLIDER);
        Socket<Double> socket = new Socket<Double>(eventBus, socketHint);

        socket.setValue(testValue);
        assertEquals(testValue, socket.getValue());
    }

    @Test
    public void testDefaultValue() throws Exception {
        SocketHint<Double> socketHint = new SocketHint<>("foo", Double.class, SocketHint.View.SLIDER, new Double[0], testValue);
        Socket<Double> socket = new Socket<Double>(eventBus, socketHint);

        assertEquals(testValue, socket.getValue());

    }

    @Test
    public void testSocketChangedEvent() throws Exception {
        SocketHint<Double> socketHint = new SocketHint<>("foo", Double.class, SocketHint.View.SLIDER);
        Socket<Double> socket = new Socket<Double>(eventBus, socketHint);

        final boolean[] handled = new boolean[]{false};
        final Double[] value = new Double[]{0.0};
        Object eventHandler = new Object() {
            @Subscribe
            public void onSocketChanged(SocketChangedEvent event) {
                handled[0] = true;
                value[0] = (Double) event.getSocket().getValue();
            }
        };

        eventBus.register(eventHandler);
        socket.setValue(testValue);
        eventBus.unregister(eventHandler);

        assertTrue(handled[0]);
        assertEquals(testValue, value[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketHintNotNull() throws Exception {
        new Socket<Double>(eventBus, null);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketEventBusNotNull() throws Exception {
        SocketHint<Double> socketHint = new SocketHint<>("foo", Double.class, SocketHint.View.SLIDER);
        new Socket<Double>(null, socketHint);
    }

    @Test(expected = ClassCastException.class)
    @SuppressWarnings("unchecked")
    public void testSocketValueWrongType() throws Exception {
        SocketHint<Double> socketHint = new SocketHint<>("foo", Double.class);
        Socket socket = new Socket<>(eventBus, socketHint);

        socket.setValue("I am not TERM1 Double");
    }
}
