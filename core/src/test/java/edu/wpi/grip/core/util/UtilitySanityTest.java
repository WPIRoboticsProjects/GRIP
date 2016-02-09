package edu.wpi.grip.core.util;


import com.google.common.testing.AbstractPackageSanityTests;

public class UtilitySanityTest extends AbstractPackageSanityTests {
    public UtilitySanityTest() {
        super();
        publicApiOnly();
        ignoreClasses(c -> c.getName().contains("Mock"));
    }

    @Override
    public void setUp() {
        SafeShutdownTest.setUpSecurityManager();
    }

    @Override
    public void tearDown() {
        SafeShutdownTest.tearDownSecurityManager();
    }
}