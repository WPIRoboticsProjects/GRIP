package edu.wpi.grip.core;


import com.google.common.eventbus.EventBus;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * An Object that can be stopped and started multiple times.
 */
public interface StartStoppable {

    /**
     * Any method that overrides this method should post a {@link edu.wpi.grip.core.events.StartedStoppedEvent}
     * to the {@link EventBus} if is successfully starts.
     *
     * @throws IOException If cleaning up some system resource fails
     */
    void start() throws IOException;

    /**
     * Any method that overrides this method should post a {@link edu.wpi.grip.core.events.StartedStoppedEvent}
     * to the {@link EventBus} if is successfully stops.
     *
     * @throws TimeoutException If the thread fails to stop in a timely manner
     * @throws IOException      If cleaning up some system resource fails.
     */
    void stop() throws TimeoutException, IOException;

    /**
     * Used to indicate if the source is running or stopped
     *
     * @return true if this source is running
     */
    boolean isStarted();
}
