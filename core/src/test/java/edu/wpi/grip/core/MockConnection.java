package edu.wpi.grip.core;


import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;

public class MockConnection<T> extends Connection<T> {

  /**
   * @param connectionValidator An object to validate that the connection can be made
   * @param outputSocket        The socket to listen for changes in.
   * @param inputSocket         A different socket to update when a change occurs in the first.
   */
  public MockConnection(EventBus eventBus, ConnectionValidator connectionValidator, @Assisted
      OutputSocket<? extends T> outputSocket, @Assisted InputSocket<T> inputSocket) {
    super(eventBus, connectionValidator, outputSocket, inputSocket);
  }
}
