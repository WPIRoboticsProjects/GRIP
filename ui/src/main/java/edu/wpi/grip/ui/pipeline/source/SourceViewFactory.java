package edu.wpi.grip.ui.pipeline.source;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;

/**
 * Factory for creating views to control sources.
 */
public final class SourceViewFactory {
    private SourceViewFactory() { /* no op */ }

    /**
     * Create an instance of {@link SourceView} appropriate for the given socket.
     *
     * @param eventBus The EventBus
     * @param source   The source to create the view for
     * @param <S>      The type of the source
     * @return The appropriate SourceView.
     */
    public static <S extends Source> SourceView<S> createSourceControlsView(EventBus eventBus, S source) {
        final SourceView<S> sourceView;
        if (source instanceof CameraSource) {
            sourceView = (SourceView<S>) new CameraSourceView(eventBus, (CameraSource) source);
        } else if (source instanceof MultiImageFileSource) {
            sourceView =  (SourceView<S>) new MultiImageFileSourceView(eventBus, (MultiImageFileSource) source);
        } else {
            sourceView = new SourceView<S>(eventBus, source) {
            };
        }
        eventBus.register(sourceView);
        return sourceView;
    }
}
