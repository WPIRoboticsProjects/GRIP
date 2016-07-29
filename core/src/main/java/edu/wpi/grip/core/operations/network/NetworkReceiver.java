package edu.wpi.grip.core.operations.network;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Manages the interface between the {@link PublishAnnotatedOperation} and the actual network
 * protocol implemented by a specific {@link Manager}.
 */
public abstract class NetworkReceiver implements AutoCloseable {

  protected final String path;

  /**
   * Create a new NetworkReceiver with the specified path.
   *
   * @param path The path of the object to get
   */
  public NetworkReceiver(String path) {
    checkArgument(!path.isEmpty(), "Name cannot be an empty string");
    this.path = path;
  }

  /**
   * Get the value of the object.
   *
   * @return The value of this NetworkReceiver
   */
  public abstract Object getValue();

  /**
   * Close the network reciever. This should not throw an exception.
   */
  public abstract void close();
}
