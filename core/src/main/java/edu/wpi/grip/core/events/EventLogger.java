package edu.wpi.grip.core.events;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;

import java.util.logging.Logger;

/**
 * Class for logging events as they're posted to the event bus.
 */
@Singleton
public class EventLogger {

  private static final Logger logger = Logger.getLogger(EventLogger.class.getName());

  @Subscribe
  public void eventPosted(LoggableEvent event) {
    final String threadName = Thread.currentThread().getName();
    logger.log(event.logLevel(),
        "Event on thread '" + threadName + "': " + event.asLoggableString());
  }

}
