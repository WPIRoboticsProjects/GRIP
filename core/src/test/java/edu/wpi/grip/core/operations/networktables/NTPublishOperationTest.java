package edu.wpi.grip.core.operations.networktables;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for error handling in the publish operations' reflection stuff.
 */
public class NTPublishOperationTest {

    private class Report implements NTPublishable {
        @NTValue(key = "foo", weight = 2)
        public double getFoo() {
            return 0.0;
        }

        @NTValue(key = "bar", weight = 1)
        public double getBar() {
            return 1.0;
        }
    }

    private class ReportWithNonDistinctWeights implements NTPublishable {
        @NTValue(key = "foo", weight = 1)
        public double getFoo() {
            return 0.0;
        }

        @NTValue(key = "bar", weight = 1)
        public double getBar() {
            return 0.0;
        }
    }

    private class ReportWithParameters implements NTPublishable {
        @NTValue(key = "foo", weight = 0)
        public double getFoo(boolean b) {
            return b ? 1.0 : 0.0;
        }
    }

    @Test
    public void testNTValueOrder() {
        NTPublishOperation<Report, Report> ntPublishOperation = new NTPublishOperation<>(Report.class);
        InputSocket<?>[] sockets = ntPublishOperation.createInputSockets(new EventBus());

        assertEquals(4, sockets.length);
        assertEquals("Publish bar", sockets[2].getSocketHint().getIdentifier());
        assertEquals("Publish foo", sockets[3].getSocketHint().getIdentifier());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonDistinctWeights() {
        new NTPublishOperation<>(ReportWithNonDistinctWeights.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParameters() {
        new NTPublishOperation<>(ReportWithParameters.class);
    }
}