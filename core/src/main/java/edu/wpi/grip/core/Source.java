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


    public <T extends Source> T start(EventBus eventBus) throws IOException {
        final T source = (T) start();
        eventBus.register(source);
        return source;
    }

    /**
     * Starts this source.
     * A source whose {@link #isRestartable()} returns true can also be stopped and started and stopped multiple times.
     *
     * @return The source object that created the camera
     * @throws IOException If the source fails to be started
     */
    protected abstract Source start() throws IOException;

    /**
     * Stops this source.
     * This will stop the source publishing new socket values after this method returns.
     * A source whose {@link #isRestartable()} returns true can also be stopped and started and stopped multiple times.
     *
     * @return The source that was stopped
     * @throws Exception
     */
    public abstract Source stop() throws Exception;

    /**
     * Used to indicate if the source is running or stopped
     *
     * @return true if this source is running
     */
    public abstract boolean isRunning();

    /**
     * Used to flag to the UI if this source should have the start/stop button displayed
     *
     * @return true if this source can be restarted once created
     */
    public abstract boolean isRestartable();
}
