package edu.wpi.grip.core.operations.network.networktables;

import edu.wpi.grip.core.operations.network.NetworkReceiver;

import edu.wpi.first.wpilibj.tables.ITable;

import java.util.function.Consumer;

public class MockNTReceiver extends NetworkReceiver {

  private final ITable table;

  /**
   * Create a new NetworkReceiver with the specified path.
   *
   * @param path  The path of the object to get
   * @param table the network table that values will be retrieved from
   */
  public MockNTReceiver(String path, ITable table) {
    super(path);
    this.table = table;
  }

  @Override
  public Object getValue() {
    return table.getValue(path);
  }

  @Override
  public void addListener(Consumer<Object> consumer) {
    // Never called
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    table.delete(path);
  }
}
