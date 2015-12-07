package edu.wpi.grip.ui.pipeline.source;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.StopStartSource;

public class SourceControlsViewFactory {
    private SourceControlsViewFactory() { /* no op */ }

    public static <T> SourceControlsView<T> createSourceControlsView(EventBus eventBus, Source source) {
        if (source instanceof StopStartSource) {
            return (SourceControlsView<T>) new StopStartSourceControlsView(eventBus, (StopStartSource) source);
        } else {
            return new SourceControlsView() {};
        }
    }
}
