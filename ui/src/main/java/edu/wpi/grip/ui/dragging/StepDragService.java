package edu.wpi.grip.ui.dragging;

import edu.wpi.grip.core.Step;

import com.google.inject.Singleton;

/**
 * Service for dragging and dropping a step.
 */
@Singleton
public class StepDragService extends DragService<Step> {

  public StepDragService() {
    super("step");
  }
}
