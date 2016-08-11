package edu.wpi.grip.ui.events;

/**
 * Toggles every steps' visibility.
 */
public class SetStepsExpandedEvent {

  private final boolean expanded;

  public SetStepsExpandedEvent(boolean expanded) {
    this.expanded = expanded;
  }

  public boolean isExpanded() {
    return expanded;
  }
}
