package edu.wpi.grip.ui.pipeline.source;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.ui.components.StartStoppableButton;
import edu.wpi.grip.ui.pipeline.OutputSocketController;
import javafx.fxml.FXML;

/**
 * Provides controls for a {@link CameraSource}
 */
public final class CameraSourceController extends SourceController<CameraSource> {

    private final StartStoppableButton.Factory startStoppableButtonFactory;

    public interface Factory {
        CameraSourceController create(CameraSource cameraSource);
    }

    @Inject
    CameraSourceController(EventBus eventBus, OutputSocketController.Factory outputSocketControllerFactory, StartStoppableButton.Factory startStoppableButtonFactory, @Assisted CameraSource cameraSource) {
        super(eventBus, outputSocketControllerFactory, cameraSource);
        this.startStoppableButtonFactory = startStoppableButtonFactory;
    }

    @FXML
    public void initialize() throws Exception {
        super.initialize();
        addControls(startStoppableButtonFactory.create(getSource()));
    }

}
