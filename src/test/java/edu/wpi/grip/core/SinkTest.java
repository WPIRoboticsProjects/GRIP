package edu.wpi.grip.core;

import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.SetSinkEvent;
import edu.wpi.grip.core.events.SocketPublishedEvent;
import edu.wpi.grip.core.events.StepAddedEvent;
import edu.wpi.grip.core.operations.PythonScriptOperation;
import edu.wpi.grip.core.sinks.DummySink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SinkTest {
    private final static class MockSink implements Sink {
        public Integer publishedValue;

        @Subscribe
        public void onSocketPublished(SocketPublishedEvent event) {
            publishedValue = (Integer) event.getSocket().getValue().get();
        }
    }

    private Optional<Throwable> throwableOptional;
    private EventBus eventBus = new EventBus((exception, context) -> throwableOptional = Optional.of(exception));
    private InputSocket<Integer> a, b;
    private OutputSocket<Integer> sum;

    @Before
    @SuppressWarnings("unchecked")
    public void createSimplePipeline() {
        this.throwableOptional = Optional.empty();
        final Pipeline pipeLine = new Pipeline(eventBus);

        final Step step = new Step(eventBus, new PythonScriptOperation(
                "import edu.wpi.grip.core as grip\n" +
                "import java.lang.Number\n" +
                "inputs = [\n" +
                "    grip.SocketHint('a', java.lang.Number, 0),\n" +
                "    grip.SocketHint('b', java.lang.Number, 0),\n" +
                "]\n" +
                "outputs = [\n" +
                "    grip.SocketHint('sum', java.lang.Number, 0.0, grip.SocketHint.View.NONE, None, True),\n" +
                "]\n" +
                "def perform(a, b): return a + b\n"));

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

        assertEquals((Integer) (123 + 456), sink.publishedValue);
    }

    @Test
    public void testSetPublishedAfter() throws Exception {
        final MockSink sink = new MockSink();
        eventBus.post(new SetSinkEvent(sink));

        this.a.setValue(123);
        this.b.setValue(456);
        this.sum.setPublished(true);

        assertEquals((Integer) (123 + 456), sink.publishedValue);
    }

    @Test
    public void testChangeSink() throws Exception {
        final MockSink sink = new MockSink();
        eventBus.post(new SetSinkEvent(sink));
        eventBus.post(new SetSinkEvent(new DummySink()));

        this.a.setValue(123);
        this.b.setValue(456);
        this.sum.setPublished(true);

        assertNotEquals((Integer) (123 + 456), sink.publishedValue);
    }
}
