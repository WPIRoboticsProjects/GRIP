package edu.wpi.grip.core.operations.networktables;


import com.google.common.testing.AbstractPackageSanityTests;

public class NetworkTablesSanityTest extends AbstractPackageSanityTests {
    public NetworkTablesSanityTest() {
        super();
        ignoreClasses(c -> c.equals(NTPublishOperation.class));
    }
}