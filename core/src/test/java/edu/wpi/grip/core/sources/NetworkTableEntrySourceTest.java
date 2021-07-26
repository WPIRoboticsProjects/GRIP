package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.operations.network.MapNetworkReceiverFactory;
import edu.wpi.grip.core.operations.network.NetworkReceiver;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.util.MockExceptionWitness;

import com.google.common.eventbus.EventBus;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetworkTableEntrySourceTest {

  private static final double TEST_NUMBER = 13.13;
  private static final String TEST_STRING = "Some test string";
  private static final String BOOLEAN_PATH = "boolean";
  private static final String NUMBER_PATH = "number";
  private static final String STRING_PATH = "string";
  private final EventBus eventBus;
  private final MockOutputSocketFactory osf;
  private NetworkTableInstance ntInstance;
  private NetworkTableEntrySource source;
  private MapNetworkReceiverFactory testingNtManager;

  public NetworkTableEntrySourceTest() {
    eventBus = new EventBus();
    osf = new MockOutputSocketFactory(eventBus);
  }

  @Before
  public void setUp() {
    ntInstance = NetworkTableInstance.create();
    NetworkTable testTable = ntInstance.getTable("GRIP/test");
    testingNtManager = p -> new NetworkReceiver(p) {
      @Override
      public Object getValue() {
        return testTable.getEntry(p).getValue().getValue();
      }

      @Override
      public void addListener(Consumer<Object> consumer) {
        // NOP for tests
      }

      @Override
      public void close() {
        testTable.delete(p);
      }
    };

    testTable.getEntry(BOOLEAN_PATH).setBoolean(true);
    testTable.getEntry(NUMBER_PATH).setDouble(TEST_NUMBER);
    testTable.getEntry(STRING_PATH).setString(TEST_STRING);
  }

  @After
  public void tearDown() {
    eventBus.post(new SourceRemovedEvent(source));
    ntInstance.close();
  }

  @Test
  public void testBoolean() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        BOOLEAN_PATH,
        NetworkTableEntrySource.Types.BOOLEAN);

    assertTrue("Socket could not be updated", source.updateOutputSockets());
    assertTrue("The socket's value was false, expected true.",
        (boolean) source.getOutputSockets().get(0).getValue().get());
  }

  @Test
  public void testBooleanWrongTypeNumber() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        NUMBER_PATH,
        NetworkTableEntrySource.Types.BOOLEAN);

    assertFalse("The socket was able to update with an invalid type", source.updateOutputSockets());
  }

  @Test
  public void testBooleanWrongTypeString() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        STRING_PATH,
        NetworkTableEntrySource.Types.BOOLEAN);

    assertFalse("The socket was able to update with an invalid type", source.updateOutputSockets());
  }

  @Test
  public void testNumber() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        NUMBER_PATH,
        NetworkTableEntrySource.Types.NUMBER);

    assertTrue("Socket could not be updated", source.updateOutputSockets());
    assertEquals("Expected numbers to be equal -- they are not equal",
        TEST_NUMBER, (double) source.getOutputSockets().get(0).getValue().get(), 0.00001);
  }

  @Test
  public void testNumberWrongTypeBoolean() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        BOOLEAN_PATH,
        NetworkTableEntrySource.Types.NUMBER);

    assertFalse("The socket was able to update with an invalid type", source.updateOutputSockets());
  }

  @Test
  public void testNumberWrongTypeString() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        STRING_PATH,
        NetworkTableEntrySource.Types.NUMBER);

    assertFalse("The socket was able to update with an invalid type", source.updateOutputSockets());
  }

  @Test
  public void testString() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        STRING_PATH,
        NetworkTableEntrySource.Types.STRING);

    assertTrue("Socket could not be updated", source.updateOutputSockets());
    assertEquals("Expected Strings to be equal -- they are not equal",
        TEST_STRING, source.getOutputSockets().get(0).getValue().get());
  }

  @Test
  public void testStringWrongTypeBoolean() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        BOOLEAN_PATH,
        NetworkTableEntrySource.Types.STRING);

    assertFalse("The socket was able to update with an invalid type", source.updateOutputSockets());
  }

  @Test
  public void testStringWrongTypeNumber() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        NUMBER_PATH,
        NetworkTableEntrySource.Types.STRING);

    assertFalse("The socket was able to update with an invalid type", source.updateOutputSockets());
  }

}
