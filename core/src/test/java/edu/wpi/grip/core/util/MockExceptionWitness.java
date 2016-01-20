package edu.wpi.grip.core.util;

import com.google.common.eventbus.EventBus;

import java.util.function.Supplier;


public class MockExceptionWitness extends ExceptionWitness {
    public static final ExceptionWitness.Factory MOCK_FACTORY = simpleFactory(EventBus::new);

    public MockExceptionWitness(EventBus eventBus, Object origin) {
        super(eventBus, origin);
    }

    public static ExceptionWitness.Factory simpleFactory(Supplier<EventBus> eventBus) {
        return origin -> new MockExceptionWitness(eventBus.get(), origin);
    }

    public static ExceptionWitness.Factory simpleFactory(EventBus eventBus) {
        return simpleFactory(() -> eventBus);
    }
}
