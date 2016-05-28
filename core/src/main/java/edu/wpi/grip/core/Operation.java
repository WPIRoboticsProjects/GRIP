package edu.wpi.grip.core;

import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;

import java.util.List;

/**
 * The common interface used by <code>Step</code>s in a pipeline to call various operations. Each instance of an
 * operation in the pipeline is handled by a unique instance of that {@code Operation} class.
 */
public interface Operation {

    /**
     * @return A list of sockets for the inputs that the operation expects.
     *
     * @implNote The returned list should be immutable (i.e. read-only)
     */
    List<InputSocket> getInputSockets();

    /**
     * @return A list of sockets for the outputs that the operation produces.
     *
     * @implNote The returned list should be immutable (i.e. read-only)
     */
    List<OutputSocket> getOutputSockets();

    /**
     * Performs this {@code Operation}.
     */
    void perform();

    /**
     * Allows the step to clean itself up when removed from the pipeline.
     * This should only be called by {@link Step#setRemoved()} to ensure correct synchronization.
     */
    default void cleanUp() {
        /* no-op */
    }
}
