package edu.wpi.grip.ui.pipeline.source;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;

/**
 * BaseSourceControllerFactory for creating views to control sources.
 */
@Singleton
public class SourceControllerFactory {
    @Inject
    private CameraSourceController.Factory cameraControllerFactory;
    @Inject
    private MultiImageFileSourceController.Factory multiImageFileSourceControllerFactory;
    @Inject
    private SourceController.BaseSourceControllerFactory<Source> baseSourceControllerFactory;

    SourceControllerFactory() { /* no-op */ }

    /**
     * Create an instance of {@link SourceController} appropriate for the given socket.
     *
     * @param source The source to create the view for
     * @param <S>    The type of the source
     * @return The appropriate SourceController.
     */
    public <S extends Source> SourceController<S> create(S source) {
        final SourceController<S> sourceController;
        if (source instanceof CameraSource) {
            sourceController = (SourceController<S>) cameraControllerFactory.create((CameraSource) source);
        } else if (source instanceof MultiImageFileSource) {
            sourceController = (SourceController<S>) multiImageFileSourceControllerFactory.create((MultiImageFileSource) source);
        } else {
            sourceController = (SourceController<S>) baseSourceControllerFactory.create(source);
        }
        return sourceController;
    }
}
