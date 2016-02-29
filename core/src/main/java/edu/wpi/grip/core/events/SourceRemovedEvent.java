package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Source;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * An event that occurs when a source is removed from the pipeline.  This is triggered by the user removing a source
 * with the GUI.
 *
 * @see Source
 */
public class SourceRemovedEvent {
    private final Source source;

    /**
     * @param source The source being removed
     */
    public SourceRemovedEvent(Source source) {
        this.source = checkNotNull(source, "Source cannot be null");
    }

    /**
     * @return The source being removed
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
}
