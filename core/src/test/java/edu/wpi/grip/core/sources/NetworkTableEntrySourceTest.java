package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.operations.network.networktables.TestingNTManager;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.util.MockExceptionWitness;

import com.google.common.eventbus.EventBus;

import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class NetworkTableEntrySourceTest {

  private EventBus eventBus;
  private MockOutputSocketFactory osf;
  private TestingNTManager testingNtManager;

  private NetworkTableEntrySource source;

  private static final double TEST_NUMBER = 13.13;
  private static final String TEST_STRING = "Some test string";
  private static final String BOOLEAN_PATH = "/GRIP/test/boolean";
  private static final String NUMBER_PATH = "/GRIP/test/number";
  private static final String STRING_PATH = "/GRIP/test/string";

  public NetworkTableEntrySourceTest() {
    eventBus = new EventBus();
    osf = new MockOutputSocketFactory(eventBus);
    testingNtManager = new TestingNTManager();

    NetworkTablesJNI.putBoolean(BOOLEAN_PATH, true);
    NetworkTablesJNI.putDouble(NUMBER_PATH, TEST_NUMBER);
    NetworkTablesJNI.putString(STRING_PATH, TEST_STRING);
  }

  @After
  public void cleanup() {
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

    Assert.assertTrue(source.updateOutputSockets());
    Assert.assertTrue((boolean) source.getOutputSockets().get(0).getValue().get());
  }

  @Test
  public void testBooleanWrongTypeNumber() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        NUMBER_PATH,
        NetworkTableEntrySource.Types.BOOLEAN);

    Assert.assertFalse(source.updateOutputSockets());
  }

  @Test
  public void testBooleanWrongTypeString() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        STRING_PATH,
        NetworkTableEntrySource.Types.BOOLEAN);

    Assert.assertFalse(source.updateOutputSockets());
  }

  @Test
  public void testNumber() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        NUMBER_PATH,
        NetworkTableEntrySource.Types.NUMBER);

    Assert.assertTrue(source.updateOutputSockets());
    Assert.assertEquals(
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

    Assert.assertFalse(source.updateOutputSockets());
  }

  @Test
  public void testNumberWrongTypeString() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        STRING_PATH,
        NetworkTableEntrySource.Types.NUMBER);

    Assert.assertFalse(source.updateOutputSockets());
  }

  @Test
  public void testString() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        STRING_PATH,
        NetworkTableEntrySource.Types.STRING);

    Assert.assertTrue(source.updateOutputSockets());
    Assert.assertEquals(TEST_STRING, source.getOutputSockets().get(0).getValue().get());
  }

  @Test
  public void testStringWrongTypeBoolean() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        BOOLEAN_PATH,
        NetworkTableEntrySource.Types.STRING);

    Assert.assertFalse(source.updateOutputSockets());
  }

  @Test
  public void testStringWrongTypeNumber() {
    source = new NetworkTableEntrySource(eventBus,
        origin -> new MockExceptionWitness(eventBus, origin),
        osf,
        testingNtManager,
        NUMBER_PATH,
        NetworkTableEntrySource.Types.STRING);

    Assert.assertFalse(source.updateOutputSockets());
  }

}
