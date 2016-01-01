package edu.wpi.grip.core;

import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.SetSinkEvent;
import edu.wpi.grip.core.events.SocketPublishedEvent;
import edu.wpi.grip.core.events.StepAddedEvent;
import edu.wpi.grip.core.sinks.DummySink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SinkTest {
    private Optional<Throwable> throwableOptional;
    private EventBus eventBus;
    private InputSocket<Integer> a, b;
    private OutputSocket<Integer> sum;

    private final static class MockSink implements Sink {
        public Double publishedValue;

        @Subscribe
        public void onSocketPublished(SocketPublishedEvent event) {
            publishedValue = (Double) event.getSocket().getValue().get();
        }
    }

    @Before
    @SuppressWarnings("unchecked")
    public void createSimplePipeline() {
        this.throwableOptional = Optional.empty();
        this.eventBus = new EventBus((exception, context) -> throwableOptional = Optional.of(exception));
        final Pipeline pipeLine = new Pipeline(eventBus);

        final Step step = new Step(eventBus, new AdditionOperation());

        this.eventBus.post(new StepAddedEvent(step));

        this.a = (InputSocket<Integer>) step.getInputSockets()[0];
        this.b = (InputSocket<Integer>) step.getInputSockets()[1];
        this.sum = (OutputSocket<Integer>) step.getOutputSockets()[0];
    }

    @After
    public void afterTest() {
        if( throwableOptional.isPresent() ) {
            throw Throwables.propagate(throwableOptional.get());
        }
    }

    @Test
    public void testSetPublished() throws Exception {
        final MockSink sink = new MockSink();
        eventBus.post(new SetSinkEvent(sink));

        this.sum.setPublished(true);
        this.a.setValue(123);
        this.b.setValue(456);

        assertEquals(Double.valueOf(123 + 456), sink.publishedValue);
    }

    @Test
    public void testSetPublishedAfter() throws Exception {
        final MockSink sink = new MockSink();
        eventBus.post(new SetSinkEvent(sink));

        this.a.setValue(123);
        this.b.setValue(456);
        this.sum.setPublished(true);

        assertEquals(Double.valueOf(123 + 456), sink.publishedValue);
    }

    @Test
    public void testChangeSink() throws Exception {
        final MockSink sink = new MockSink();
        eventBus.post(new SetSinkEvent(sink));
        eventBus.post(new SetSinkEvent(new DummySink()));

        this.a.setValue(123);
        this.b.setValue(456);
        this.sum.setPublished(true);

        assertNotEquals((123 + 456), sink.publishedValue);
    }
}
