package edu.wpi.grip.core.operations.network;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.MockInputSocketFactory;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

/**
 * Tests for error handling in the publish operations' reflection stuff.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class PublishAnnotatedOperationTest {

    public static class SimpleReport implements Publishable {
        public static final String KEY_1 = "bar";
        public static final String KEY_2 = "foo";

        @PublishValue(key = KEY_2, weight = 2)
        public double getFoo() {
            return 0.0;
        }

        @PublishValue(key = KEY_1, weight = 1)
        public double getBar() {
            return 1.0;
        }
    }

    public static class ReportWithNonDistinctWeights implements Publishable {
        @PublishValue(key = "foo", weight = 1)
        public double getFoo() {
            return 0.0;
        }

        @PublishValue(key = "bar", weight = 1)
        public double getBar() {
            return 0.0;
        }
    }

    public static class ReportWithNonDistinctKeys implements Publishable {
        @PublishValue(key = "fooBar", weight = 1)
        public double getFoo() {
            return 0.0;
        }

        @PublishValue(key = "fooBar", weight = 2)
        public double getBar() {
            return 1.0;
        }
    }

    public static class ReportWithParameters implements Publishable {
        @PublishValue(key = "foo", weight = 0)
        public double getFoo(boolean b) {
            return b ? 1.0 : 0.0;
        }
    }

    public static class ReportWithMultipleEmptyKeys implements Publishable {
        @PublishValue(weight = 1)
        public double getFoo() {
            return 0.0;
        }

        @PublishValue(weight = 2)
        public double getBar() {
            return 1.0;
        }
    }

    public static class ReportWithMixedEmptyAndSuppliedKeys implements Publishable {
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

    public static class ReportWithPrivateMethod implements Publishable {
        @PublishValue(weight = 1)
        private double getFoo() {
            throw new UnsupportedOperationException("Should not be called");
        }
    }

    static class StaticNonPublicReport implements Publishable {
        @PublishValue(weight = 1)
        public double getFoo() {
            throw new UnsupportedOperationException("Should not be called");
        }
    }

    public class NonStaticPublicReport implements Publishable {
        @PublishValue(weight = 1)
        public double getFoo() {
            throw new UnsupportedOperationException("Should not be called");
        }
    }

    public static class NoAnnotatedMethodReport implements Publishable {
        /*
         * Intentionally unannotated.
         */
        public double getFoo() {
            throw new UnsupportedOperationException("Should not be called");
        }
    }

    static class TestPublishAnnotatedOperation<T extends Publishable> extends PublishAnnotatedOperation<T, T> {

        public TestPublishAnnotatedOperation(Class<T> type, MapNetworkPublisherFactory factory) {
            super(new MockInputSocketFactory(new EventBus()), type, type, Function.identity(), factory);
        }

        public TestPublishAnnotatedOperation(Class<T> type) {
            this(type, MockMapNetworkPublisher::new);
        }
    }


    @Test
    public void testNTValueOrder() {
        Operation ntPublishOperation = new TestPublishAnnotatedOperation<>(SimpleReport.class);
        List<InputSocket> sockets = ntPublishOperation.getInputSockets();

        assertEquals("Unexpected number of sockets", 4, sockets.size());
        assertEquals("Wrong publish name", "Publish bar", sockets.get(2).getSocketHint().getIdentifier());
        assertEquals("Wrong publish name", "Publish foo", sockets.get(3).getSocketHint().getIdentifier());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonDistinctWeights() {
        new TestPublishAnnotatedOperation<>(ReportWithNonDistinctWeights.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithMethodThatHasParameters() {
        new TestPublishAnnotatedOperation<>(ReportWithParameters.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithNonDistinctKeys() {
        new TestPublishAnnotatedOperation<>(ReportWithNonDistinctKeys.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithMultipleEmptyKeys() {
        new TestPublishAnnotatedOperation<>(ReportWithMultipleEmptyKeys.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithMixedEmptyAndSuppliedKeys() {
        new TestPublishAnnotatedOperation<>(ReportWithMixedEmptyAndSuppliedKeys.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithPrivateMethod() {
        new TestPublishAnnotatedOperation<>(ReportWithPrivateMethod.class);
    }

    @Test(expected = IllegalAccessError.class)
    public void testNonPublicPublishable() {
        new TestPublishAnnotatedOperation<>(StaticNonPublicReport.class);
    }

    @Test(expected = IllegalAccessError.class)
    public void testNonStaticInnerPublishable() {
        new TestPublishAnnotatedOperation<>(NonStaticPublicReport.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithNoAnnoatedMethods() {
        new TestPublishAnnotatedOperation<>(NoAnnotatedMethodReport.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishSimpleReportReturnsExpectedValues() {
        final String PUBLISHER_NAME = "PUBLISH QUACKERY!";
        final boolean[] publishNameChangedRan = {false};
        final boolean[] doPublishRan = {false};
        class TestMapNetworkPublisher<T> extends MapNetworkPublisher<T> {
            public TestMapNetworkPublisher(Set keys) {
                super(keys);
            }

            @Override
            protected void publishNameChanged(Optional oldName, String newName) {
                assertEquals("Name was not what was expected", newName, PUBLISHER_NAME);
                assertFalse("There was an old name when there shouldn't have been", oldName.isPresent());
                publishNameChangedRan[0] = true;
            }

            @Override
            public void close() {
                fail("Close should not have run!");
            }

            @Override
            protected void doPublish(Map<String, T> publishMap) {
                assertTrue("The first key didn't get passed to the map", publishMap.containsKey(SimpleReport.KEY_1));
                assertTrue("The second key didn't get passed to the map", publishMap.containsKey(SimpleReport.KEY_2));
                doPublishRan[0] = true;
            }

            @Override
            protected void doPublishSingle(T value) {
                fail("This should not have run");
            }

            @Override
            protected void doPublish() {
                fail("This should not have run");
            }
        }
        // Cannot be method reference due to JDK/javac bug 8144673
        final MapNetworkPublisherFactory factory = new MapNetworkPublisherFactory() {
            @Override
            public <T> MapNetworkPublisher<T> create(Set<String> keys) {
                return new TestMapNetworkPublisher<>(keys);
            }
        };
        final TestPublishAnnotatedOperation<SimpleReport> testPublishAnnotatedOperation = new TestPublishAnnotatedOperation<>(SimpleReport.class, factory);

        final List<InputSocket> inputSockets = testPublishAnnotatedOperation.getInputSockets();
        inputSockets.get(0).setValue(new SimpleReport());
        inputSockets.get(1).setValue(PUBLISHER_NAME);

        testPublishAnnotatedOperation.perform();

        assertTrue("publishNameChanged never ran", publishNameChangedRan[0]);
        assertTrue("doPublish never ran", doPublishRan[0]);
    }

    @Test
    public void testPublishProperlyResolvesSocketType() {
        TestPublishAnnotatedOperation<SimpleReport> testPublishAnnotatedOperation
                = new TestPublishAnnotatedOperation<>(SimpleReport.class);
        assertEquals("Socket types were not the same",
                testPublishAnnotatedOperation.getSocketType(),
                SimpleReport.class);
    }
}
