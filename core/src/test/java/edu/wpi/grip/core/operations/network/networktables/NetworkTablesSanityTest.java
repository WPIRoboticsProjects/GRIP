package edu.wpi.grip.core.operations.network.networktables;


import com.google.common.testing.AbstractPackageSanityTests;

import edu.wpi.first.networktables.NetworkTableInstance;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class NetworkTablesSanityTest extends AbstractPackageSanityTests {

  public NetworkTablesSanityTest() {
    super();
    setDefault(NetworkTableInstance.class, NetworkTableInstance.create());
  }

}
