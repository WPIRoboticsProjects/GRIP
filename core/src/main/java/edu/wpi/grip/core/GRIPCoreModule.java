package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.serialization.Project;

/**
 * A Guice {@link com.google.inject.Module} for GRIP's core package.  This is where instances of {@link Pipeline},
 * {@link Palette}, {@link Project}, etc... are created.
 */
public class GRIPCoreModule extends AbstractModule {

    private final EventBus eventBus = new EventBus(this::onSubscriberException);

    public GRIPCoreModule() {
        Thread.setDefaultUncaughtExceptionHandler(this::onThreadException);
    }

    @Override
    protected void configure() {
        // Register any injected object on the event bus
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                encounter.register((InjectionListener<I>) eventBus::register);
            }
        });

        bind(EventBus.class).toInstance(eventBus);
    }

    private void onSubscriberException(Throwable exception, SubscriberExceptionContext context) {
        eventBus.post(new UnexpectedThrowableEvent(exception, "An event subscriber threw an exception"));
    }

    private void onThreadException(Thread thread, Throwable exception) {
        eventBus.post(new UnexpectedThrowableEvent(exception, thread + " threw an exception"));
    }
}
