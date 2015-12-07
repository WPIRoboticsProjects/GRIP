package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * Base class for an input into the pipeline.
 */
public abstract class Source {

    /**
     * @return The name of this source.  This is used by the GUI to distinguish different sources.  For example,
     * {@link edu.wpi.grip.core.sources.ImageFileSource} returns the filename of the image.
     */
    public abstract String getName();

    /**
     * Get the sockets for this source.
     *
     * @return @return An array of {@link OutputSocket}s for the outputs that the source produces.
     */
    public final OutputSocket[] getOutputSockets() {
        final OutputSocket[] outputSockets = this.createOutputSockets();
        for (OutputSocket socket : outputSockets) {
            socket.setSource(Optional.of(this));
        }

        return outputSockets;
    }

    protected abstract OutputSocket[] createOutputSockets();

    /**
     * @return A {@link Properties} containing data that can be used to re-create this source.  This is used for
     * serialization/deserialization.
     */
    public abstract Properties getProperties();

    /**
     * Load this source from a set of properties.
     *
     * @see #getProperties()
     */
    public abstract void createFromProperties(EventBus eventBus, Properties properties) throws IOException;
}
