package edu.wpi.grip.core.events;

import edu.wpi.grip.core.Source;

/**
 * An event that occurs when a source is stopped.
 */
public class SourceStoppedEvent {
    private final Source source;

    public SourceStoppedEvent(Source source){
        this.source = source;
    }

    public Source getSource() {
        return this.source;
    }
}