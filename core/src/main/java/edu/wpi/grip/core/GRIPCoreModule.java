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
import edu.wpi.grip.core.settings.SettingsProvider;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.InputSocketImpl;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.OutputSocketImpl;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.GRIPMode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.logging.*;

/**
 * A Guice {@link com.google.inject.Module} for GRIP's core package.  This is where instances of {@link Pipeline},
 * {@link Palette}, {@link Project}, etc... are created.
 */
@SuppressWarnings("PMD.MoreThanOneLogger")
public class GRIPCoreModule extends AbstractModule {
    private static final Logger logger = Logger.getLogger(GRIPCoreModule.class.getName());

    private final EventBus eventBus;

    // This is in a static initialization block so that we don't create a ton of
    // log files when running tests
    static {
        //Set up the global level logger. This handles IO for all loggers.
        final Logger globalLogger = LogManager.getLogManager().getLogger("");

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
            globalLogger.config("GRIP Version: " + edu.wpi.grip.core.Main.class.getPackage().getImplementationVersion());

        } catch (IOException exception) {//Something happened setting up file IO
            throw new IllegalStateException("Failed to configure the Logger", exception);
        }
    }

    /*
     * This class should not be used in tests. Use GRIPCoreTestModule for tests.
     */
    public GRIPCoreModule() {
        this.eventBus = new EventBus(this::onSubscriberException);
        // TODO: HACK! Don't assign the global thread handler to an instance method. Creates global state.
        Thread.setDefaultUncaughtExceptionHandler(this::onThreadException);
    }

    @Override
    protected void configure() {
        bind(GRIPMode.class).toInstance(GRIPMode.HEADLESS);

        // Register any injected object on the event bus
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                encounter.register((InjectionListener<I>) eventBus::register);
            }
        });

        bind(EventBus.class).toInstance(eventBus);

        // Allow for just injecting the settings provider, instead of the whole pipeline
        bind(SettingsProvider.class).to(Pipeline.class);

        install(new FactoryModuleBuilder().build(new TypeLiteral<Connection.Factory<Object>>() {
        }));

        bind(ConnectionValidator.class).to(Pipeline.class);
        bind(Source.SourceFactory.class).to(Source.SourceFactoryImpl.class);

        bind(InputSocket.Factory.class).to(InputSocketImpl.FactoryImpl.class);
        bind(OutputSocket.Factory.class).to(OutputSocketImpl.FactoryImpl.class);
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

    protected void onSubscriberException(Throwable exception, @Nullable SubscriberExceptionContext exceptionContext) {
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
    protected void onThreadException(Thread thread, Throwable exception) {
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
