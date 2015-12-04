package edu.wpi.grip.core.sinks;


import com.google.common.eventbus.EventBus;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.events.SocketPublishedEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NetworkTablesSinkTest {
    final private EventBus eventBus = new EventBus(((exception, context) -> exception.printStackTrace()));
    private NetworkTablesSink networkTablesSink;
    private NetworkTable networkTable;

    @Before
    public void setUp() {
        NetworkTable.globalDeleteAll();
        networkTablesSink = new NetworkTablesSink(eventBus);
        networkTable = NetworkTable.getTable(NetworkTablesSink.TABLE_NAME);
    }

    @Test
    public void testNumberValueEndsUpOnNetworkTables() {
        // Given
        final String identifier = "IDENTIFIER";
        final Number initialValue = 5.0;
        // When
        testWithSocketHint(identifier, Number.class, initialValue);

        // Then
        assertEquals("Network tables did not return value stored with socket", initialValue.doubleValue(), networkTable.getNumber(identifier), 0);
    }

    @Test
    public void testArrayNumberValueEndsUpOnNetworkTables() {
        // Given
        final String identifier = "IDENTIFIER";
        final Number initialValue[] = new Number[]{
                0,
                Math.PI,
                -3.0,
                5.0,
                7.0,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.NaN,
                Double.MAX_VALUE,
                -Double.MAX_VALUE,
                Double.MAX_VALUE
        };

        // When
        testWithSocketHint(identifier, Number[].class, initialValue);

        // Then
        final double storedValue[] = networkTable.getNumberArray(identifier);
        for (int i = 0; i < initialValue.length; i++) {
            assertEquals("Network tables did not return value stored with socket", initialValue[i].doubleValue(), storedValue[i], 0);
        }
    }

    private void testWithSocketHint(String identifier, Class clazz, Object initialValue) {
        eventBus.post(new SocketPublishedEvent(new OutputSocket(eventBus, new SocketHint(identifier, clazz, initialValue, null, null, true))));
    }
}