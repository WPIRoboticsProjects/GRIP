package edu.wpi.grip.ui.pipeline.source;

import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ClassifierSource;
import edu.wpi.grip.core.sources.HttpSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;
import edu.wpi.grip.core.sources.VideoFileSource;

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
  private MultiImageFileSourceController.Factory multiImageControllerFactory;
  @Inject
  private HttpSourceController.Factory httpSourceControllerFactory;
  @Inject
  private SourceController.BaseSourceControllerFactory<Source> baseSourceControllerFactory;
  @Inject
  private ClassifierSourceController.Factory fileSourceControllerFactory;
  @Inject
  private VideoFileSourceController.Factory videoFileControllerFactory;

  SourceControllerFactory() { /* no-op */ }

  /**
   * Create an instance of {@link SourceController} appropriate for the given socket.
   *
   * @param source The source to create the view for
   * @param <S>    The type of the source
   *
   * @return The appropriate SourceController.
   */
  @SuppressWarnings("unchecked")
  public <S extends Source> SourceController<S> create(S source) {
    final SourceController<S> sourceController;
    if (source instanceof CameraSource) {
      sourceController = (SourceController<S>) cameraControllerFactory.create((CameraSource)
          source);
    } else if (source instanceof MultiImageFileSource) {
      sourceController = (SourceController<S>) multiImageControllerFactory.create(
          (MultiImageFileSource) source);
    } else if (source instanceof HttpSource) {
      sourceController = (SourceController<S>) httpSourceControllerFactory.create(
          (HttpSource) source);
    } else if (source instanceof ClassifierSource) {
      sourceController = (SourceController<S>) fileSourceControllerFactory.create(
          (ClassifierSource) source);
    } else if (source instanceof VideoFileSource) {
      sourceController = (SourceController<S>) videoFileControllerFactory.create(
          (VideoFileSource) source);
    } else {
      sourceController = (SourceController<S>) baseSourceControllerFactory.create(source);
    }
    return sourceController;
  }
}
