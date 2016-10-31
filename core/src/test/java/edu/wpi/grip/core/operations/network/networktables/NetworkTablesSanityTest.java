package edu.wpi.grip.core.operations.network.networktables;


import com.google.common.testing.AbstractPackageSanityTests;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class NetworkTablesSanityTest extends AbstractPackageSanityTests {

  public NetworkTablesSanityTest() {
    super();
    ignoreClasses(MockNetworkTable.class::equals);
    ignoreClasses(MockNTReceiver.class::equals);
  }

}
