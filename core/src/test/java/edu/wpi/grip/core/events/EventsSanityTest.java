package edu.wpi.grip.core.events;

import edu.wpi.grip.core.metrics.MockTimer;
import edu.wpi.grip.core.metrics.Timer;

import com.google.common.testing.AbstractPackageSanityTests;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class EventsSanityTest extends AbstractPackageSanityTests {
  // The tests are all in the superclass.
  public EventsSanityTest() {
    super();
    setDefault(Timer.class, MockTimer.MOCK_FACTORY.create(this));
  }
}
