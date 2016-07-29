package edu.wpi.grip.core.operations.network;

@SuppressWarnings("PMD.UncommentedEmptyMethodBody")
public class MockNetworkReceiver extends NetworkReceiver implements MapNetworkReceiverFactory {

  public MockNetworkReceiver() {
    super("/Test/path");
  }

  public MockNetworkReceiver(String path) {
    super(path);
  }

  @Override
  public Object getValue() {
    return null;
  }

  @Override
  public void close() {

  }

  @Override
  public NetworkReceiver create(String path) {
    return new MockNetworkReceiver(path);
  }
}
