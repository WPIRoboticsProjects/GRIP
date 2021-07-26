
package edu.wpi.grip.ui.pipeline.source;

import edu.wpi.grip.core.sources.HttpSource;
import edu.wpi.grip.ui.components.ExceptionWitnessResponderButton;
import edu.wpi.grip.ui.pipeline.OutputSocketController;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Provides controls for a {@link HttpSource}.
 */
public class HttpSourceController extends SourceController<HttpSource> {

  public interface Factory {

    HttpSourceController create(HttpSource source);
  }

  @Inject
  HttpSourceController(
      final EventBus eventBus,
      final OutputSocketController.Factory outputSocketControllerFactory,
      final ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory,
      @Assisted final HttpSource source) {
    super(eventBus, outputSocketControllerFactory, exceptionWitnessResponderButtonFactory, source);
  }

}
