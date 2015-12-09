package edu.wpi.grip.core.events;


import edu.wpi.grip.core.StartStoppable;

/**
 * An event that occurs when a {@link StartStoppable StartStoppable's} state changes.
 */
public class StartedStoppedEvent {
    private final StartStoppable startStoppable;

    public StartedStoppedEvent(StartStoppable startStoppable) {
        this.startStoppable = startStoppable;
    }

    public StartStoppable getStartStoppable() {
        return this.startStoppable;
    }
}
