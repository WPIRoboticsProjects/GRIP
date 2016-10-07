package edu.wpi.grip.ui.pipeline.source;

import edu.wpi.grip.core.sources.FileSource;
import edu.wpi.grip.ui.components.ExceptionWitnessResponderButton;
import edu.wpi.grip.ui.pipeline.OutputSocketController;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Provides controls for a {@link edu.wpi.grip.core.sources.FileSource}.
 */
public class FileSourceController extends SourceController<FileSource> {

  public interface Factory {
    FileSourceController create(FileSource source);
  }

  @Inject
  FileSourceController(
      EventBus eventBus,
      OutputSocketController.Factory outputSocketControllerFactory,
      ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory,
      @Assisted FileSource source) {
    super(eventBus, outputSocketControllerFactory, exceptionWitnessResponderButtonFactory, source);
  }

}
