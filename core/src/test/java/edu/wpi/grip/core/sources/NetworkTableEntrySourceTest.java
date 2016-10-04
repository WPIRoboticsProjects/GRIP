package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.operations.network.MapNetworkReceiverFactory;
import edu.wpi.grip.core.operations.network.networktables.MockNTReceiver;
import edu.wpi.grip.core.operations.network.networktables.MockNetworkTable;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.util.MockExceptionWitness;

import com.google.common.eventbus.EventBus;

import edu.wpi.first.wpilibj.tables.ITable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetworkTableEntrySourceTest {

  private final EventBus eventBus;
  private final MockOutputSocketFactory osf;

  private NetworkTableEntrySource source;
  private MapNetworkReceiverFactory testingNtManager;

  private static final double TEST_NUMBER = 13.13;
  private static final String TEST_STRING = "Some test string";
  private final ITable testTable = MockNetworkTable.getTable("GRIP/test");
  private static final String BOOLEAN_PATH = "boolean";
  private static final String NUMBER_PATH = "number";
  private static final String STRING_PATH = "string";

  public NetworkTableEntrySourceTest() {
    eventBus = new EventBus();
    osf = new MockOutputSocketFactory(eventBus);
  }

  @Before
  public void setUp() {
    testingNtManager = p -> new MockNTReceiver(p, testTable);

    testTable.putBoolean(BOOLEAN_PATH, true);
    testTable.putDouble(NUMBER_PATH, TEST_NUMBER);
    testTable.putString(STRING_PATH, TEST_STRING);
  }

  @After
  public void tearDown() {
    eventBus.post(new SourceRemovedEvent(source));
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
