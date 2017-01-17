package edu.wpi.grip.core.events;

/**
 * An event that can potentially dirty the save file.
 *
 * <p>These events ensure that anything that changes causes the save file to be flagged as dirty and
 * in need of being saved for the project to be deemed "clean" again.
 */
public interface DirtiesSaveEvent {

  /**
   * Some events may have more logic regarding whether they make the save dirty or not.
   *
   * @return True if this event should dirty the project save
   */
  default boolean doesDirtySave() {
    return true;
  }
}
