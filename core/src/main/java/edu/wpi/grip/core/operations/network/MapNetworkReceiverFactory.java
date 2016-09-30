package edu.wpi.grip.core.operations.network;


/**
 * A factory to create {@link NetworkReceiver NetworkRecievers}.
 */
@FunctionalInterface
public interface MapNetworkReceiverFactory {
  NetworkReceiver create(String path);
}
