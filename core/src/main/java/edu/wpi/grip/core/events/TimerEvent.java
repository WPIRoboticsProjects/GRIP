package edu.wpi.grip.core.events;

import edu.wpi.grip.core.metrics.Analysis;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event fired when a {@link edu.wpi.grip.core.metrics.Timer Timer} finishes timing something.
 *
 * <p>This contains:
 * <ul>
 * <li>The object being timed</li>
 * <li>How long the timed action took, in microseconds</li>
 * <li>Historical data about the action</li>
 * </ul>
 * </p>
 */
public class TimerEvent {

  private final Object target;
  private final long elapsedTime;
  private final Analysis data;

  /**
   * Creates a new timer event.
   *
   * @param target      the object that was timed
   * @param elapsedTime the time elapsed in microseconds
   * @param data        the analysis data of the event
   */
  public TimerEvent(Object target, long elapsedTime, Analysis data) {
    checkNotNull(target, "target");
    checkNotNull(data, "data");
    this.target = target;
    this.elapsedTime = elapsedTime;
    this.data = data;
  }

  /**
   * Gets the target object that had an action being timed.
   */
  public Object getTarget() {
    return target;
  }

  /**
   * Gets the elapsed time of the action, in microseconds.
   */
  public long getElapsedTime() {
    return elapsedTime;
  }

  /**
   * Gets the analysis of the historical timing data for the action.
   */
  public Analysis getData() {
    return data;
  }
}
