package edu.wpi.grip.core.operations.network;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * Tests for error handling in the publish operations' reflection stuff.
 */
public class KeyValuePublishOperationTest {

    private class Report implements Publishable {
        @PublishValue(key = "foo", weight = 2)
        public double getFoo() {
            return 0.0;
        }

        @PublishValue(key = "bar", weight = 1)
        public double getBar() {
            return 1.0;
        }
    }

    private class ReportWithNonDistinctWeights implements Publishable {
        @PublishValue(key = "foo", weight = 1)
        public double getFoo() {
            return 0.0;
        }

        @PublishValue(key = "bar", weight = 1)
        public double getBar() {
            return 0.0;
        }
    }

    private class ReportWithNonDistinctKeys implements Publishable {
        @PublishValue(key = "fooBar", weight = 1)
        public double getFoo() {
            return 0.0;
        }

        @PublishValue(key = "fooBar", weight = 2)
        public double getBar() {
            return 1.0;
        }
    }

    private class ReportWithParameters implements Publishable {
        @PublishValue(key = "foo", weight = 0)
        public double getFoo(boolean b) {
            return b ? 1.0 : 0.0;
        }
    }

    private class ReportWithMultipleEmptyKeys implements Publishable {
        @PublishValue(weight = 1)
        public double getFoo() {
            return 0.0;
        }

        @PublishValue(weight = 2)
        public double getBar() {
            return 1.0;
        }
    }

    private class ReportWithMixedEmptyAndSuppliedKeys implements Publishable {
        @PublishValue(key = "foo", weight = 1)
        public double getFoo() {
            return 0.0;
        }

        @PublishValue(key = "bar", weight = 2)
        public double getBar() {
            return 1.0;
        }

        @PublishValue(weight = 3)
        public double getFooBar() {
            return 3.0;
        }
    }

    private static class TestKeyValuePublishOperation<S, T extends Publishable> extends KeyValuePublishOperation<S, T> {

        public TestKeyValuePublishOperation(Manager manager, Class<T> type) {
            super(manager, type);
        }

        public TestKeyValuePublishOperation(Manager manager, Class<S> socketType, Class<T> reportType, Function<S, T> converter) {
            super(manager, socketType, reportType, converter);
        }

        @Override
        protected String getNetworkProtocolNameAcronym() {
            return "TP";
        }

        @Override
        protected String getNetworkProtocolName() {
            return "Test Protocol";
        }

        @Override
        protected String getSocketHintStringPrompt() {
            return "Test Name";
        }
    }


    @Test
    public void testNTValueOrder() {
        TestKeyValuePublishOperation<Report, Report> ntPublishOperation = new TestKeyValuePublishOperation<>(new MockManager(), Report.class);
        InputSocket<?>[] sockets = ntPublishOperation.createInputSockets(new EventBus());

        assertEquals(4, sockets.length);
        assertEquals("Publish bar", sockets[2].getSocketHint().getIdentifier());
        assertEquals("Publish foo", sockets[3].getSocketHint().getIdentifier());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonDistinctWeights() {
        new TestKeyValuePublishOperation<>(new MockManager(), ReportWithNonDistinctWeights.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithMethodThatHasParameters() {
        new TestKeyValuePublishOperation<>(new MockManager(), ReportWithParameters.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithNonDistinctKeys() {
        new TestKeyValuePublishOperation<>(new MockManager(), ReportWithNonDistinctKeys.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithMultipleEmptyKeys() {
        new TestKeyValuePublishOperation<>(new MockManager(), ReportWithMultipleEmptyKeys.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithMixedEmptyAndSuppliedKeys() {
        new TestKeyValuePublishOperation<>(new MockManager(), ReportWithMixedEmptyAndSuppliedKeys.class);
    }

}