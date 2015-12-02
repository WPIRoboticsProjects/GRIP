package edu.wpi.grip.core;

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
    private SocketHint<Double> sh;
    private OutputSocket<Double> socket;

    @Before
    public void initialize(){
        sh = new SocketHint<>("foo", Double.class, 0.0, SocketHint.View.SLIDER);
        socket = new OutputSocket<Double>(eventBus, sh);
    }

    @Test
    public void testGetSocketHint() throws Exception {
        assertEquals("foo", socket.getSocketHint().getIdentifier());
        assertEquals(Double.class, socket.getSocketHint().getType());
        assertEquals(SocketHint.View.SLIDER, socket.getSocketHint().getView());
    }

    @Test
    public void testSetValue() throws Exception {
        socket.setValue(testValue);
        assertEquals(testValue, socket.getValue().get());
    }

    @Test
    public void testDefaultValue() throws Exception {
        sh = new SocketHint<>("foo", Double.class, testValue, SocketHint.View.SLIDER);
        socket = new OutputSocket<Double>(eventBus, sh);
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
                value[0] = (Double) e.getSocket().getValue().get();
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
        SocketHint<Double> sh = new SocketHint<>("foo", Double.class, 0.0);
        OutputSocket<Double> socket = new OutputSocket<Double>(eventBus, sh);

        final boolean[] handled = new boolean[]{false};
        Object eventHandler = new Object() {
            @Subscribe
            public void onSocketPreviewed(SocketPreviewChangedEvent e) {
                handled[0] = true;
                assertTrue("A preview event fired for a socket but the socket was not labeled as able to be previewed",
                        e.getSocket().isPreviewed());
            }
        };

        eventBus.register(eventHandler);
        socket.setPreviewed(true);
        eventBus.unregister(eventHandler);

        assertTrue("SocketPreviewChangedEvent was not received", handled[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketHintNotNullInput() throws Exception {
        new InputSocket<Double>(eventBus, null);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketHintNotNullOutput() throws Exception {
        new OutputSocket<Double>(eventBus, null);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketEventBusNotNullInput() throws Exception {
        new InputSocket<Double>(null, sh);
    }

    @Test(expected = NullPointerException.class)
    public void testSocketEventBusNotNullOutput() throws Exception {
        new OutputSocket<Double>(null, sh);
    }

    @Test(expected = ClassCastException.class)
    @SuppressWarnings("unchecked")
    public void testSocketValueWrongType() throws Exception {
        InputSocket socket = new InputSocket(eventBus, sh);

        socket.setValue("I am not a Double");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotPublishableSocket() throws Exception {
        final SocketHint<Double> sh = new SocketHint<>("foo", Double.class, 0.0);
        final OutputSocket<Double> socket = new OutputSocket<Double>(eventBus, sh);

        socket.setPublished(true);
    }
}
