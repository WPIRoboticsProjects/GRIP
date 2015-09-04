package edu.wpi.grip.core;

/**
 * Common interface for an input into the pipeline.
 */
public interface Source {

    /**
     * Get the sockets for this source.
     * @return @return An array of {@link Socket}s for the outputs that the source produces.
     */
    Socket[] getOutputSockets();
}
