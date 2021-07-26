package edu.wpi.grip.core;

import edu.wpi.grip.core.cuda.AccelerationMode;
import edu.wpi.grip.core.cuda.CudaDetector;
import edu.wpi.grip.core.cuda.CudaVerifier;
import edu.wpi.grip.core.cuda.NullAccelerationMode;
import edu.wpi.grip.core.cuda.NullCudaDetector;
import edu.wpi.grip.core.events.EventLogger;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.metrics.BenchmarkRunner;
import edu.wpi.grip.core.metrics.Timer;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.settings.SettingsProvider;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.InputSocketImpl;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.OutputSocketImpl;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ClassifierSource;
import edu.wpi.grip.core.sources.HttpSource;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;
import edu.wpi.grip.core.sources.NetworkTableEntrySource;
import edu.wpi.grip.core.sources.VideoFileSource;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.GripMode;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * A Guice {@link com.google.inject.Module} for GRIP's core package.  This is where instances of
 * {@link Pipeline}, {@link Palette}, {@link Project}, etc... are created.
 */
@SuppressWarnings({"PMD.MoreThanOneLogger", "PMD.CouplingBetweenObjects"})
public class GripCoreModule extends AbstractModule {

  private static final Logger logger = Logger.getLogger(GripCoreModule.class.getName());
  private final EventBus eventBus;

  /*
   * This class should not be used in tests. Use GRIPCoreTestModule for tests.
   */
  @SuppressWarnings("JavadocMethod")
  public GripCoreModule() {
    this.eventBus = new EventBus(this::onSubscriberException);
    // TODO: HACK! Don't assign the global thread handler to an instance method. Creates global
    // state.
    Thread.setDefaultUncaughtExceptionHandler(this::onThreadException);
  }

  @Override
  protected void configure() {
    bind(GripMode.class).toInstance(GripMode.HEADLESS);

    // Register any injected object on the event bus
    bindListener(Matchers.any(), new TypeListener() {
      @Override
      public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        encounter.register((InjectionListener<I>) eventBus::register);
      }
    });

    bind(EventBus.class).toInstance(eventBus);
    bind(EventLogger.class).asEagerSingleton();

    // Allow for just injecting the settings provider, instead of the whole pipeline
    bind(SettingsProvider.class).to(Pipeline.class);

    install(new FactoryModuleBuilder().build(new TypeLiteral<Connection.Factory<Object>>() {
    }));

    bind(StepIndexer.class).to(Pipeline.class);
    bind(ConnectionValidator.class).to(Pipeline.class);
    bind(Source.SourceFactory.class).to(Source.SourceFactoryImpl.class);

    // Bind CUDA-specific stuff to default values
    // These will be overridden by the GripCudaModule at app runtime, but this lets
    // automated tests assume CPU-only operation modes
    bind(CudaDetector.class).to(NullCudaDetector.class);
    bind(AccelerationMode.class).to(NullAccelerationMode.class);
    bind(CudaVerifier.class).in(Scopes.SINGLETON);
    bind(Properties.class)
        .annotatedWith(Names.named("cudaProperties"))
        .toInstance(new Properties());

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
    install(new FactoryModuleBuilder()
        .implement(HttpSource.class, HttpSource.class)
        .build(HttpSource.Factory.class));
    install(new FactoryModuleBuilder()
        .implement(NetworkTableEntrySource.class, NetworkTableEntrySource.class)
        .build(NetworkTableEntrySource.Factory.class));
    install(new FactoryModuleBuilder()
        .implement(ClassifierSource.class, ClassifierSource.class)
        .build(ClassifierSource.Factory.class));
    install(new FactoryModuleBuilder()
        .implement(VideoFileSource.class, VideoFileSource.class)
        .build(VideoFileSource.Factory.class));

    install(new FactoryModuleBuilder().build(ExceptionWitness.Factory.class));
    install(new FactoryModuleBuilder().build(Timer.Factory.class));

    bind(BenchmarkRunner.class).asEagerSingleton();

    bind(Cleaner.class).asEagerSingleton();
  }

  protected void onSubscriberException(Throwable exception, @Nullable SubscriberExceptionContext
      exceptionContext) {
    if (exception instanceof InterruptedException) {
      logger.log(Level.FINE, "EventBus Subscriber threw InterruptedException", exception);
      Thread.currentThread().interrupt();
    } else {
      logger.log(Level.SEVERE, "An event subscriber threw an exception", exception);
      eventBus.post(new UnexpectedThrowableEvent(exception, "An event subscriber threw an "
          + "exception on thread '" + Thread.currentThread().getName() + "'"));
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
        // This can potentially happen before the main class has even been loaded to handle these
        // exceptions
        logger.log(Level.SEVERE, "Uncaught Exception on thread " + thread, exception);
        final UnexpectedThrowableEvent event = new UnexpectedThrowableEvent(exception, thread
            + " threw an exception");
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
