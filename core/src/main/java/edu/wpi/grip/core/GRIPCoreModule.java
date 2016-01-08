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

import java.io.IOException;
import java.util.logging.*;

/**
 * A Guice {@link com.google.inject.Module} for GRIP's core package.  This is where instances of {@link Pipeline},
 * {@link Palette}, {@link Project}, etc... are created.
 */
public class GRIPCoreModule extends AbstractModule {
    private final Logger logger = Logger.getLogger(GRIPCoreModule.class.getName());

    private final EventBus eventBus = new EventBus(this::onSubscriberException);

    public GRIPCoreModule() {
        //Set up the global level logger. This handles IO for all loggers.
        final Logger globalLogger = LogManager.getLogManager().getLogger("");//This is our global logger

        try {
            // Remove the default handlers that stream to System.err
            for (Handler handler : globalLogger.getHandlers()) {
                globalLogger.removeHandler(handler);
            }

            final Handler fileHandler = new FileHandler("%h/GRIP.log");//Log to the file "GRIPlogger.log"

            //Set level to handler and logger
            fileHandler.setLevel(Level.FINE);
            globalLogger.setLevel(Level.FINE);

            // We need to stream to System.out instead of System.err
            final StreamHandler sh = new StreamHandler(System.out, new SimpleFormatter()) {

                @Override
                public synchronized void publish(final LogRecord record) {
                    super.publish(record);
                    // For some reason this doesn't happen automatically.
                    // This will ensure we get all of the logs printed to the console immediately
                    // when running on a remote device.
                    flush();
                }
            };
            sh.setLevel(Level.CONFIG);

            globalLogger.addHandler(sh); // Add stream handler

            globalLogger.addHandler(fileHandler);//Add the handler to the global logger

            fileHandler.setFormatter(new SimpleFormatter());//log in text, not xml

            globalLogger.config("Configuration done.");//Log that we are done setting up the logger

        } catch (IOException exception) {//Something happened setting up file IO
            throw new IllegalStateException("Failed to configure the Logger", exception);
        }

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
            logger.log(Level.SEVERE, "An event subscriber threw an exception", exception);
            eventBus.post(new UnexpectedThrowableEvent(exception, "An event subscriber threw an exception"));
        }
    }

    /*
     * We intentionally catch the throwable because we can't be sure what will happen.
     * We drop the last throwable because we clearly have a problem beyond our control.
     */
    @SuppressWarnings({"PMD.AvoidCatchingThrowable", "PMD.EmptyCatchBlock"})
    private void onThreadException(Thread thread, Throwable exception) {
        // Don't do anything outside of a try catch block when dealing with thread death
        try {
            if (exception instanceof Error && !(exception instanceof AssertionError)) {
                // Try, this may not work. but its worth a shot.
                logger.log(Level.SEVERE, "Error from " + thread, exception);
            } else if (exception instanceof InterruptedException) {
                logger.log(Level.FINE, "InterruptedException from thread " + thread, exception);
                Thread.currentThread().interrupt();
            } else {
                // This can potentially happen before the main class has even been loaded to handle these exceptions
                logger.log(Level.SEVERE, "Uncaught Exception on thread " + thread, exception);
                final UnexpectedThrowableEvent event = new UnexpectedThrowableEvent(exception, thread + " threw an exception");
                eventBus.post(event);
                // It is possible that the event was never handled.
                // If it wasn't we want to perform the shutdown hook here.
                event.shutdownIfFatal();
            }
        } catch (Throwable throwable) {
            try {
                logger.log(Level.SEVERE, "Failed to handle thread exception", throwable);
            } catch (Throwable throwable1) {
                // Seriously, just give up at this point.
            }
        }
        // Don't do anything outside of a try catch block when dealing with thread death
    }
}
