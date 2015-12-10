package edu.wpi.grip.ui.pipeline.source;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.ui.components.StartStoppableButton;

/**
 * Provides controls for a {@link CameraSource}
 */
public final class CameraSourceView extends SourceView<CameraSource> {

    public CameraSourceView(EventBus eventBus, CameraSource cameraSource) {
        super(eventBus, cameraSource);
        addControls(new StartStoppableButton(eventBus, cameraSource));
    }

}
