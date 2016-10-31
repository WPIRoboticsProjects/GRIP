package edu.wpi.grip.core.events;

import edu.wpi.grip.core.metrics.Timer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event fired when a {@link edu.wpi.grip.core.metrics.Timer Timer} finishes timing something.
 *
 * <p>This contains:
 * <ul>
 * <li>The timer posting the event</li>
 * <li>The object being timed</li>
 * <li>How long the timed action took, in microseconds</li>
 * <li>Historical data about the action</li>
 * </ul>
 * </p>
 */
public class TimerEvent {

  private final Timer timer;
  private final Object target;
  private final long elapsedTime;

  /**
   * Creates a new timer event.
   *
   * @param timer       the timer that posted this event
   * @param target      the object that was timed
   * @param elapsedTime the time elapsed in microseconds
   */
  public TimerEvent(Timer timer, Object target, long elapsedTime) {
    checkNotNull(timer, "timer");
    checkNotNull(target, "target");
    this.timer = timer;
    this.target = target;
    this.elapsedTime = elapsedTime;
  }

  /**
   * Gets the {@code Timer} that posted this event.
   */
  public Timer getTimer() {
    return timer;
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

}
