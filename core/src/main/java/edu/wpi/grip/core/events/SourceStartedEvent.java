package edu.wpi.grip.core.events;


import edu.wpi.grip.core.Source;

/**
 * An event that occurs when a source is started.
 */
public class SourceStartedEvent {
    private final Source source;

    public SourceStartedEvent(Source source){
        this.source = source;
    }

    public Source getSource() {
        return this.source;
    }
}
