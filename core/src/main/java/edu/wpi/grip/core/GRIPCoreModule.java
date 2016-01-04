package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;
import edu.wpi.grip.core.util.ExceptionWitness;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Guice {@link com.google.inject.Module} for GRIP's core package.  This is where instances of {@link Pipeline},
 * {@link Palette}, {@link Project}, etc... are created.
 */
public class GRIPCoreModule extends AbstractModule {
    private final Logger logger = Logger.getLogger(GRIPCoreModule.class.getName());

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

        install(new FactoryModuleBuilder().build(new TypeLiteral<Connection.Factory<Object>>() {
        }));


        bind(Source.SourceFactory.class).to(Source.SourceFactoryImpl.class);
        bind(CameraSource.FrameGrabberFactory.class).to(CameraSource.FrameGrabberFactoryImpl.class);
        install(new FactoryModuleBuilder()
                .implement(CameraSource.class, CameraSource.class)
                .build(CameraSource.Factory.class));
        install(new FactoryModuleBuilder()
                .implement(ImageFileSource.class, ImageFileSource.class)
                .build(ImageFileSource.Factory.class));
        install(new FactoryModuleBuilder()
                .implement(MultiImageFileSource.class, MultiImageFileSource.class)
                .build(MultiImageFileSource.Factory.class));

        install(new FactoryModuleBuilder().build(ExceptionWitness.Factory.class));
    }

    private void onSubscriberException(Throwable exception, SubscriberExceptionContext context) {
        if (exception instanceof InterruptedException) {
            logger.log(Level.FINE, "EventBus Subscriber threw InterruptedException", exception);
            Thread.currentThread().interrupt();
        } else {
            eventBus.post(new UnexpectedThrowableEvent(exception, "An event subscriber threw an exception"));
        }
    }

    private void onThreadException(Thread thread, Throwable exception) {
        if (exception instanceof InterruptedException) {
            logger.log(Level.FINE, "InterruptedException from thread " + thread, exception);
            Thread.currentThread().interrupt();
        } else {
            eventBus.post(new UnexpectedThrowableEvent(exception, thread + " threw an exception"));
        }
    }
}
