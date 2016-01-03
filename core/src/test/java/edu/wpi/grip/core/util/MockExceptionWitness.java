package edu.wpi.grip.core.util;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;


public class MockExceptionWitness extends ExceptionWitness {

    public MockExceptionWitness(EventBus eventBus, @Assisted Object origin) {
        super(eventBus, origin);
    }
}
