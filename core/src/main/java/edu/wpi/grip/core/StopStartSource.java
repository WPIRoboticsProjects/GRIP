package edu.wpi.grip.core;


import com.google.common.eventbus.EventBus;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * A Source that can be stopped and started multiple times.
 */
public abstract class StopStartSource extends Source {

    /**
     * Starts this source.
     *
     * @return The source object that created the camera
     * @throws IOException If the source fails to be started
     */
    public <T extends Source> T start(EventBus eventBus) throws IOException {
        start();
        eventBus.register(this);
        return (T) this;
    }

    /**
     * Any method that overrides this method should post a {@link edu.wpi.grip.core.events.SourceStartedEvent}
     * to the {@link EventBus} if is successfully starts.
     * @throws IOException If the source fails to be started
     */
    protected abstract void start() throws IOException;

    /**
     * Stops this source.
     * This will stop the source publishing new socket values after this method returns.
     *
     * Any method that overrides this method should post a {@link edu.wpi.grip.core.events.SourceStoppedEvent}
     * to the {@link EventBus} if is successfully stops.
     *
     * @return The source that was stopped
     * @throws TimeoutException if the thread running the source fails to stop.
     * @throws IOException If there is a problem stopping the Source
     */
    public abstract void stop() throws TimeoutException, IOException;

    /**
     * Used to indicate if the source is running or stopped
     *
     * @return true if this source is running
     */
    public abstract boolean isRunning();
}
