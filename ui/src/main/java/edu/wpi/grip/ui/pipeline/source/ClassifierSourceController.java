package edu.wpi.grip.ui.pipeline.source;

import edu.wpi.grip.core.sources.ClassifierSource;
import edu.wpi.grip.ui.components.ExceptionWitnessResponderButton;
import edu.wpi.grip.ui.pipeline.OutputSocketController;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Provides controls for a {@link ClassifierSource}.
 */
public class ClassifierSourceController extends SourceController<ClassifierSource> {

  public interface Factory {
    ClassifierSourceController create(ClassifierSource source);
  }

  @Inject
  ClassifierSourceController(
      EventBus eventBus,
      OutputSocketController.Factory outputSocketControllerFactory,
      ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory,
      @Assisted ClassifierSource source) {
    super(eventBus, outputSocketControllerFactory, exceptionWitnessResponderButtonFactory, source);
  }

}
