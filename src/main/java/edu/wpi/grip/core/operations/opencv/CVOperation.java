package edu.wpi.grip.core.operations.opencv;

import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Socket;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface CVOperation extends Operation {

    @Override
    default Optional<InputStream> getIcon() {
        return Optional.of(
                getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/opencv.png")
        );
    }

    /**
     * Determines what socket or sockets are erring and updates their status to reflect this
     *
     * @param throwable     The exception that was thrown when this operation was performed
     * @param inputSockets  The input sockets to the operation
     * @param outputSockets The output sockets to the operation
     */
    default void updateSocketErrorState(Throwable throwable, InputSocket<?>[] inputSockets, OutputSocket<?>[] outputSockets) {
        List<Socket> invalidSockets = new ArrayList(inputSockets.length + outputSockets.length);

        // Find possible problem values
        for (InputSocket input : inputSockets) {
            if (throwable.getMessage().contains(input.getSocketHint().getIdentifier())) {
                invalidSockets.add(input);
            }
        }
        for (OutputSocket output : outputSockets) {
            if (throwable.getMessage().contains(output.getSocketHint().getIdentifier())) {
                invalidSockets.add(output);
            }
        }

        if (invalidSockets.size() == 1) {
            // If we only have one then we are pretty sure that its the invalid socket
            invalidSockets.forEach(socket -> socket.setStatus(Socket.SocketStatus.INVALID));
        } else if (!invalidSockets.isEmpty()) {
            // We know that we have a few possible invalid sockets
            invalidSockets.forEach(socket -> socket.setStatus(Socket.SocketStatus.POSSIBLE_INVALID));
        } else {
            // We don't know which one is the invalid socket so mark all of them as invalid
            for (InputSocket<?> inputSocket : inputSockets) {
                inputSocket.setStatus(Socket.SocketStatus.POSSIBLE_INVALID);
            }
            for (OutputSocket<?> outputSocket : outputSockets) {
                outputSocket.setStatus(Socket.SocketStatus.POSSIBLE_INVALID);
            }
        }
    }

    /**
     * Marks all of the sockets as valid
     *
     * @param inputSockets  from the perform function
     * @param outputSockets from the perform function
     */
    default void updateSocketValidState(InputSocket<?>[] inputSockets, OutputSocket<?>[] outputSockets) {
        for (InputSocket<?> inputSocket : inputSockets) {
            inputSocket.setStatus(Socket.SocketStatus.VALID);
        }
        for (OutputSocket<?> outputSocket : outputSockets) {
            outputSocket.setStatus(Socket.SocketStatus.VALID);
        }
    }
}
