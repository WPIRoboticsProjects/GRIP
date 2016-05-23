package edu.wpi.grip.core.sockets;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.core.events.SocketPreviewChangedEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SocketTest {
    private final EventBus eventBus = new EventBus();
    private final Double testValue = 12345.6789;
    private SocketHint<Number> sh;
    private OutputSocket<Number> socket;

    @Before
    public void initialize() {
        sh = SocketHints.Inputs.createNumberSliderSocketHint("foo", 0.0, 0.0, 1.0);
        socket = new OutputSocketImpl<>(eventBus, sh);
        eventBus.register(socket);
    }

    @Test
    public void testGetSocketHint() throws Exception {
        assertEquals("foo", socket.getSocketHint().getIdentifier());
        assertEquals(Number.class, socket.getSocketHint().getType());
        assertEquals(SocketHint.View.SLIDER, socket.getSocketHint().getView());
    }

    @Test
    public void testSetValue() throws Exception {
        socket.setValue(testValue);
        assertEquals(testValue, socket.getValue().get());
    }

    @Test
    public void testDefaultValue() throws Exception {
        sh = SocketHints.Inputs.createNumberSliderSocketHint("foo", testValue, 0.0, 1.0);
        socket = new OutputSocketImpl<>(eventBus, sh);
        assertEquals(testValue, socket.getValue().get());

    }

    @Test
    public void testSocketChangedEvent() throws Exception {
        final boolean[] handled = new boolean[]{false};
        final Double[] value = new Double[]{0.0};
        Object eventHandler = new Object() {
            @Subscribe
            public void onSocketChanged(SocketChangedEvent e) {
                handled[0] = true;
                value[0] = (Double) socket.getValue().get();
            }
        };

        eventBus.register(eventHandler);
        socket.setValue(testValue);
        eventBus.unregister(eventHandler);

        assertTrue(handled[0]);
        assertEquals(testValue, value[0]);
    }

    @Test
    public void testSocketPreview() {
        SocketHint<Number> sh = SocketHints.createNumberSocketHint("foo", 0);
        OutputSocket<Number> socket = new OutputSocketImpl<>(eventBus, sh);

        final boolean[] handled = new boolean[]{false};
        Object eventHandler = new Object() {
            @Subscribe
            public void onSocketPreviewed(SocketPreviewChangedEvent e) {
                handled[0] = true;
                assertTrue("A preview event fired for a socket but the socket was not labeled as able to be previewed",
                        socket.isPreviewed());
            }
        };

        eventBus.register(eventHandler);
        socket.setPreviewed(true);
        eventBus.unregister(eventHandler);

        assertTrue("SocketPreviewChangedEvent was not received", handled[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketHintNotNullInput() throws Exception {
        new InputSocketImpl<>(eventBus, null);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketHintNotNullOutput() throws Exception {
        new OutputSocketImpl<>(eventBus, null);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketEventBusNotNullInput() throws Exception {
        new InputSocketImpl<>(null, sh);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketEventBusNotNullOutput() throws Exception {
        new OutputSocketImpl<>(null, sh);
    }

    @Test(expected = ClassCastException.class)
    @SuppressWarnings("unchecked")
    public void testSocketValueWrongType() throws Exception {
        InputSocket socket = new InputSocketImpl(eventBus, sh);

        socket.setValue("I am not a Double");
    }
}
