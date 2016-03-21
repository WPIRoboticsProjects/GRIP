package edu.wpi.grip.ui.util;


import com.google.common.eventbus.EventBus;

import java.util.logging.Logger;

public class MockGripPlatform extends GRIPPlatform {
    public MockGripPlatform(EventBus eventBus) {
        super(eventBus, Logger.getLogger(MockGripPlatform.class.getName()));
    }
}
