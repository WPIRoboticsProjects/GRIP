package edu.wpi.grip.core.operations.network.ros;


import edu.wpi.grip.core.operations.network.NetworkPublisher;

import org.ros.internal.message.Message;
import org.ros.message.MessageFactory;

/**
 * A publisher that uses {@link ROSMessagePublisher.Converter} to resolve how the node should
 * publish messages.
 */
public abstract class ROSMessagePublisher extends NetworkPublisher<ROSMessagePublisher.Converter> {

  @FunctionalInterface
  public interface Converter {
    /**
     * @param m              The message to store the data in.
     * @param messageFactory A factory to create anny additional messages needed.
     */
    void convert(Message m, MessageFactory messageFactory);
  }

}
