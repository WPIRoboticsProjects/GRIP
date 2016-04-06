package edu.wpi.grip.core.sockets;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Step;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides a pairing of a list of {@link InputSocket} and {@link OutputSocket}
 * that an {@link Operation} produces when a {@link Step} is initialized.
 */
public final class SocketsProvider {
    private final InputSocket<?>[] inputSockets;
    private final OutputSocket<?>[] outputSockets;

    public SocketsProvider(InputSocket<?>[] inputSockets, OutputSocket<?>[] outputSockets) {
        this.inputSockets = checkNotNull(inputSockets, "InputSockets cannot be null");
        this.outputSockets = checkNotNull(outputSockets, "OutputSockets cannot be null");
    }

    public final InputSocket<?>[] inputSockets() {
        return inputSockets;
    }

    public final OutputSocket<?>[] outputSockets() {
        return outputSockets;
    }
}
