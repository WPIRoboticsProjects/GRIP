package edu.wpi.grip.ui.pipeline.source;


import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.sources.MultiImageFileSource;
import edu.wpi.grip.ui.components.ExceptionWitnessResponderButton;
import edu.wpi.grip.ui.components.PreviousNextButtons;
import edu.wpi.grip.ui.pipeline.OutputSocketController;
import javafx.fxml.FXML;

/**
 * Provides controls for a {@link MultiImageFileSource}
 */
public final class MultiImageFileSourceController extends SourceController<MultiImageFileSource> {

    public interface Factory {
        MultiImageFileSourceController create(MultiImageFileSource multiImageFileSource);
    }

    @Inject
    MultiImageFileSourceController(
            final EventBus eventBus,
            final OutputSocketController.Factory outputSocketControllerFactory,
            final ExceptionWitnessResponderButton.Factory exceptionWitnessResponderButtonFactory,
            @Assisted final MultiImageFileSource multiImageFileSource) {
        super(eventBus, outputSocketControllerFactory, exceptionWitnessResponderButtonFactory, multiImageFileSource);
    }

    @FXML
    public void initialize() throws Exception {
        super.initialize();
        addControls(new PreviousNextButtons(getSource()));
    }
}
