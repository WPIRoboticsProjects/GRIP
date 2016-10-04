package edu.wpi.grip.core.operations.network.networktables;

import edu.wpi.grip.core.operations.network.NetworkReceiver;

import java.util.function.Consumer;

public class MockNTReceiver extends NetworkReceiver {

  /**
   * Create a new NetworkReceiver with the specified path.
   *
   * @param path The path of the object to get
   */
  public MockNTReceiver(String path) {
    super(path);
  }

  @Override
  public Object getValue() {
    return MockNetworkTable.getTable("/").getValue(path);
  }

  @Override
  public void addListener(Consumer<Object> consumer) {
    // Never called
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    MockNetworkTable.getTable("/").delete(path);
  }
}
