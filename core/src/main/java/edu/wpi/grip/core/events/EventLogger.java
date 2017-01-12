package edu.wpi.grip.core.events;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;

import java.util.logging.Logger;

/**
 * Class for logging events as they're posted to the event bus.
 */
@Singleton
public class EventLogger {

  private static final Logger logger = Logger.getLogger("EventLogger");

  @Subscribe
  public void eventPosted(Object event) {
    final String threadName = Thread.currentThread().getName();
    LoggableEvent annotation = event.getClass().getAnnotation(LoggableEvent.class);
    if (annotation != null) {
      logger.log(annotation.level().getLogLevel(),
          "Event on thread '" + threadName + "': " + event);
    }
  }

}
