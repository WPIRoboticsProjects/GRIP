package edu.wpi.grip.core.events;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;

import java.util.logging.Logger;

/**
 * Class for logging events as they're posted to the event bus. This should be the
 * <i>first</i> object registered to the event bus to avoid potential exceptions from being thrown
 * by other event subscribers.
 */
@Singleton
public class EventLogger {

  private static final Logger logger = Logger.getLogger("EventLogger");

  @Subscribe
  public void eventPosted(Object event) {
    final String threadName = Thread.currentThread().getName();
    if (event.getClass().isAnnotationPresent(LoggableEvent.class)) {
      logger.info("Event on thread '" + threadName + "': " + event);
    }
  }

}
