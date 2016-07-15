package edu.wpi.grip.ui.util;


import com.google.common.eventbus.EventBus;

public class MockGripPlatform extends GripPlatform {
  public MockGripPlatform(EventBus eventBus) {
    super(eventBus);
  }
}
