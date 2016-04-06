package edu.wpi.grip.core.operations.network;

import com.google.common.eventbus.EventBus;
import com.google.common.reflect.TypeToken;
import edu.wpi.grip.core.sockets.InputSocket;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    abstract static class TestPublishAnnotatedOperation<S, T extends Publishable> extends PublishAnnotatedOperation<S, T, Double> {

        public TestPublishAnnotatedOperation(MapNetworkPublisherFactory factory) {
            super(factory);
        }

        public TestPublishAnnotatedOperation() {
            this(MockMapNetworkPublisher::new);
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
        TestPublishAnnotatedOperation<SimpleReport, SimpleReport> ntPublishOperation = new TestPublishAnnotatedOperation<SimpleReport, SimpleReport>() {
        };
        InputSocket<?>[] sockets = ntPublishOperation.createInputSockets(new EventBus());

        assertEquals("Unexpected number of sockets", 4, sockets.length);
        assertEquals("Wrong publish name", "Publish bar", sockets[2].getSocketHint().getIdentifier());
        assertEquals("Wrong publish name", "Publish foo", sockets[3].getSocketHint().getIdentifier());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonDistinctWeights() {
        new TestPublishAnnotatedOperation<ReportWithNonDistinctWeights, ReportWithNonDistinctWeights>() {
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithMethodThatHasParameters() {
        new TestPublishAnnotatedOperation<ReportWithParameters, ReportWithParameters>() {

        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithNonDistinctKeys() {
        new TestPublishAnnotatedOperation<ReportWithNonDistinctKeys, ReportWithNonDistinctKeys>() {
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithMultipleEmptyKeys() {
        new TestPublishAnnotatedOperation<ReportWithMultipleEmptyKeys, ReportWithMultipleEmptyKeys>() {
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithMixedEmptyAndSuppliedKeys() {
        new TestPublishAnnotatedOperation<ReportWithMixedEmptyAndSuppliedKeys, ReportWithMixedEmptyAndSuppliedKeys>() {
        };
    }

    @Test(expected = IllegalAccessError.class)
    public void testPublishableWithPrivateMethod() {
        new TestPublishAnnotatedOperation<ReportWithPrivateMethod, ReportWithPrivateMethod>() {
        };
    }

    @Test(expected = IllegalAccessError.class)
    public void testNonPublicPublishable() {
        new TestPublishAnnotatedOperation<StaticNonPublicReport, StaticNonPublicReport>() {
        };
    }

    @Test(expected = IllegalAccessError.class)
    public void testNonStaticInnerPublishable() {
        new TestPublishAnnotatedOperation<NonStaticPublicReport, NonStaticPublicReport>() {
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublishableWithNoAnnoatedMethods() {
        new TestPublishAnnotatedOperation<NoAnnotatedMethodReport, NoAnnotatedMethodReport>() {
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public <T extends Publishable> void testUnresolvableTypesFails() {
        // This should not be able to resolve the type and as such should fail
        new TestPublishAnnotatedOperation<T, T>() {
        };
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
        final MapNetworkPublisherFactory factory = new MapNetworkPublisherFactory() {
            @Override
            public <T> MapNetworkPublisher<T> create(Set<String> keys) {
                return new TestMapNetworkPublisher<>(keys);
            }
        };
        final TestPublishAnnotatedOperation testPublishAnnotatedOperation = new TestPublishAnnotatedOperation<SimpleReport, SimpleReport>(factory) {
        };

        final InputSocket[] inputSockets = testPublishAnnotatedOperation.createInputSockets(new EventBus());
        inputSockets[0].setValue(new SimpleReport());
        inputSockets[1].setValue(PUBLISHER_NAME);
        final Optional<?> data = testPublishAnnotatedOperation.createData();

        testPublishAnnotatedOperation.perform(inputSockets, null, data);

        assertTrue("publishNameChanged never ran", publishNameChangedRan[0]);
        assertTrue("doPublish never ran", doPublishRan[0]);
    }

    @Test
    public void testPublishProperlyResolvesSocketType() {
        TestPublishAnnotatedOperation<SimpleReport, SimpleReport> testPublishAnnotatedOperation
                = new TestPublishAnnotatedOperation<SimpleReport, SimpleReport>() {
        };
        assertEquals("Socket types were not the same",
                testPublishAnnotatedOperation.getSocketType(),
                TypeToken.of(SimpleReport.class));
    }
}