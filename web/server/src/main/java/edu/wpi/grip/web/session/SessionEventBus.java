package edu.wpi.grip.web.session;


import com.google.common.eventbus.EventBus;


public class SessionEventBus extends EventBus {

  public SessionEventBus(final String identifier) {
    super(identifier);
  }
}
