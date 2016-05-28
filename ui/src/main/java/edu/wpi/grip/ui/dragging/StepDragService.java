package edu.wpi.grip.ui.dragging;

import com.google.inject.Singleton;
import edu.wpi.grip.core.Step;

/**
 * Service for dragging and dropping a step
 */
@Singleton
public class StepDragService extends DragService<Step> {

    public StepDragService() {
        super("step");
    }
}
