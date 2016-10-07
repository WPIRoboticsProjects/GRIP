package edu.wpi.grip.ui.pipeline.source;

import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ClassifierSource;
import edu.wpi.grip.core.sources.HttpSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
  private HttpSourceController.Factory httpSourceControllerFactory;
  @Inject
  private SourceController.BaseSourceControllerFactory<Source> baseSourceControllerFactory;
  @Inject
  private ClassifierSourceController.Factory fileSourceControllerFactory;

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
      sourceController = (SourceController<S>) cameraControllerFactory.create((CameraSource)
          source);
    } else if (source instanceof MultiImageFileSource) {
      sourceController = (SourceController<S>) multiImageFileSourceControllerFactory.create(
          (MultiImageFileSource) source);
    } else if (source instanceof HttpSource) {
      sourceController = (SourceController<S>) httpSourceControllerFactory.create(
          (HttpSource) source);
    } else if (source instanceof ClassifierSource) {
      sourceController = (SourceController<S>) fileSourceControllerFactory.create(
          (ClassifierSource) source);
    } else {
      sourceController = (SourceController<S>) baseSourceControllerFactory.create(source);
    }
    return sourceController;
  }
}
