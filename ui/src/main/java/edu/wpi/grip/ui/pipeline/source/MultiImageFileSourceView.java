package edu.wpi.grip.ui.pipeline.source;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sources.MultiImageFileSource;
import edu.wpi.grip.ui.components.PreviousNextButtons;

/**
 * Provides controls for a {@link MultiImageFileSource}
 */
public final class MultiImageFileSourceView extends SourceView<MultiImageFileSource> {

    public MultiImageFileSourceView(final EventBus eventBus, final MultiImageFileSource source) {
        super(eventBus, source);
        addControls(new PreviousNextButtons(source));
    }
}
