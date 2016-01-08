package edu.wpi.grip.core.operations.networktables;

/**
 * Mock instance of the NTManager to be used in tests
 */
public class MockNTManager extends NTManager {
    public MockNTManager() {
        super(null, () -> null);
    }
}
