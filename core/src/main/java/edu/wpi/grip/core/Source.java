package edu.wpi.grip.core;

/**
 * Common interface for an input into the pipeline.
 */
public interface Source {

    /**
     * @return The name of this source.  This is used by the GUI to distinguish different sources.  For example,
     * {@link edu.wpi.grip.core.sources.ImageFileSource} returns the filename of the image.
     */
    String getName();

    /**
     * Get the sockets for this source.
     * @return @return An array of {@link OutputSocket}s for the outputs that the source produces.
     */
    OutputSocket[] getOutputSockets();
}
