package edu.wpi.grip.core;

import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;

/**
 * Allows {@link Connection Connections} to be validated to ensure that the pipeline will remain
 * valid.
 */
public interface ConnectionValidator {

  /**
   * Resolves which {@link Socket} is the {@link OutputSocket} and which is the {@link InputSocket}
   * and calls {@link #canConnect(OutputSocket, InputSocket)}.
   *
   * @param socket1 The first socket
   * @param socket2 The second socket
   * @return The return value of {@link #canConnect(OutputSocket, InputSocket)}
   */
  default boolean canConnect(Socket socket1, Socket socket2) {
    // One socket must be an input and one must be an output
    if (socket1.getDirection() == socket2.getDirection()) {
      return false;
    }

    final OutputSocket<?> outputSocket;
    final InputSocket<?> inputSocket;
    if (socket1.getDirection().equals(Socket.Direction.OUTPUT)) {
      outputSocket = (OutputSocket) socket1;
      inputSocket = (InputSocket) socket2;
    } else {
      inputSocket = (InputSocket) socket1;
      outputSocket = (OutputSocket) socket2;
    }

    // DO NOT DO ANY OTHER SORT OF VALIDATION HERE. We just want to resolve the types.

    return canConnect(outputSocket, inputSocket);
  }

  /**
   * Determines if an output socket can be connected to an input socket.
   *
   * @param outputSocket The output socket to connect to the input socket
   * @param inputSocket  The input socket to accept the output value of the output socket
   * @return True if a valid connection can be made from these two Sockets
   */
  boolean canConnect(OutputSocket<?> outputSocket, InputSocket<?> inputSocket);
}
