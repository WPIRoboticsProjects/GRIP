package edu.wpi.grip.core.operations.network;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
    checkNotNull(path, "Path cannot be null");
    checkArgument(!path.isEmpty(), "Path cannot be an empty string");
    this.path = path;
  }

  /**
   * Get the value of the object.
   *
   * @return The value of this NetworkReceiver
   */
  public abstract Object getValue();

  /**
   * Add a listener to the NetworkReceiver item.
   *
   * @param consumer The consumer to call when this item has a update
   */
  public abstract void addListener(Consumer<Object> consumer);

  /**
   * Close the network reciever. This should not throw an exception.
   */
  public abstract void close();
}
