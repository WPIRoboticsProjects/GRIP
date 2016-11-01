package edu.wpi.grip.ui.pipeline.source;

import edu.wpi.grip.core.sources.VideoFileSource;
import edu.wpi.grip.ui.components.ExceptionWitnessResponderButton;
import edu.wpi.grip.ui.pipeline.OutputSocketController;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class VideoFileSourceController extends SourceController<VideoFileSource> {

  @Inject
  VideoFileSourceController(
      EventBus eventBus,
      OutputSocketController.Factory outputSocketControllerFactory,
      ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory,
      @Assisted VideoFileSource source) {
    super(eventBus, outputSocketControllerFactory, exceptionWitnessResponderButtonFactory, source);
  }

  public interface Factory {
    VideoFileSourceController create(VideoFileSource source);
  }

}
