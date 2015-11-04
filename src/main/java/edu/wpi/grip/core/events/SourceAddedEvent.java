package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Source;


/**
 * An event that occurs when a new source is added to the pipeline.  This is triggered by the user adding a source with
 * the GUI.
 *
 * @see Source
 */
public class SourceAddedEvent implements AddedEvent {
    private final Source source;

    /**
     * @param source The source being added
     */
    public SourceAddedEvent(Source source) {
        this.source = source;
    }

    /**
     * @return The source being added.
     */
    public Source getSource() {
        return this.source;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("source", source)
                .toString();
    }

    @Override
    public SourceRemovedEvent createUndoEvent() {
        return new SourceRemovedEvent(source);
    }
}
