package edu.wpi.grip.util;


import edu.wpi.grip.core.FileManager;
import edu.wpi.grip.core.GripCoreModule;
import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.PipelineRunner;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.GripServerTest;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.MockFrameGrabberFactory;
import edu.wpi.grip.core.util.MockFileManager;

import com.google.common.eventbus.SubscriberExceptionContext;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This GRIPCoreTestModule is designed to be used in tests. Any thread that uses the
 * DefaultUncaughtExceptionHandler will have exceptions dumped into this class. Exceptions thrown by
 * the event bus will also be collected using this handler as well. When {@link #tearDown()} is
 * called the exceptions that have been accrued during the test will be rethrown. This should be
 * done in a tests {@link org.junit.After} annotated method. If tear down is not called and another
 * TestThrowablesHandler is created this class will throw an assertion error. This is done to ensure
 * that exceptions always get dumped for the test that has just run.
 */
public class GripCoreTestModule extends GripCoreModule {
  @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
      justification = "Member is volatile")
  private static volatile boolean instanceAlive = false;

  private final ConcurrentLinkedQueue<ThreadThrowablePair> threadExceptions = new
      ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<SubscriberThrowablePair> subscriberExceptions = new
      ConcurrentLinkedQueue<>();
  private boolean setUp = false;

  public GripCoreTestModule() {
    super();
    assert !instanceAlive : "There is a GripCoreTestModule that did not have it's `tearDown` "
        + "method called.";
  }

  @SuppressWarnings("PMD.JUnit4TestShouldUseBeforeAnnotation")
  public void setUp() {
    instanceAlive = true;
    setUp = true;
  }

  /**
   * Rethrows any exceptions that were thrown by other threads or the event bus. This must be called
   * in the {@link org.junit.After} method for any test that uses this class. If this is not called
   * then the next test that tries to use an instance of this class will throw an exception.
   */
  @SuppressWarnings("PMD.JUnit4TestShouldUseAfterAnnotation")
  public void tearDown() {
    try {
      final List<Throwable> throwables = new ArrayList<>(2);
      if (!threadExceptions.isEmpty()) {
        throwables.add(MultiException.create(threadExceptions.stream()
            .map(ThreadThrowablePair::getThrowable).collect(Collectors.toList())));
      }
      if (!subscriberExceptions.isEmpty()) {
        throwables.add(MultiException.create(subscriberExceptions.stream()
            .map(SubscriberThrowablePair::getThrowable).collect(Collectors.toList())));
      }
      if (!throwables.isEmpty()) {
        throw MultiException.create(throwables);
      }
    } finally {
      instanceAlive = false;
    }
  }


  @Override
  protected void configure() {
    assert setUp : "The GripCoreTestModule handler was not set up. Call 'setUp' before passing "
        + "the injector";
    bind(CameraSource.FrameGrabberFactory.class).to(MockFrameGrabberFactory.class);
    bind(FileManager.class).to(MockFileManager.class);
    super.configure();
    // HTTP server injection bindings
    bind(GripServer.JettyServerFactory.class).to(GripServerTest.TestServerFactory.class);
    bind(GripServer.class).asEagerSingleton();
    bind(PipelineRunner.class).to(ManualPipelineRunner.class);
  }

  @Override
  protected void onThreadException(Thread thread, Throwable exception) {
    // Do not call super, we don't want the default functionality
    threadExceptions.add(new ThreadThrowablePair(thread, exception));
  }

  @Override
  protected void onSubscriberException(Throwable exception, SubscriberExceptionContext
      exceptionContext) {
    // Do not call super, we don't want the default functionality
    subscriberExceptions.add(new SubscriberThrowablePair(exception, exceptionContext));
  }

  private static final class ThreadThrowablePair {
    private final Thread thread;
    private final Throwable throwable;

    private ThreadThrowablePair(Thread thread, Throwable throwable) {
      this.thread = checkNotNull(thread, "Thread cannot be null");
      this.throwable = checkNotNull(throwable, "Throwable cannot be null");
    }

    private Throwable getThrowable() {
      return throwable;
    }
  }

  private static final class SubscriberThrowablePair {
    private final Throwable exception;
    private final SubscriberExceptionContext exceptionContext;

    private SubscriberThrowablePair(Throwable exception, SubscriberExceptionContext
        exceptionContext) {
      this.exception = checkNotNull(exception, "Throwable cannot be null");
      this.exceptionContext = checkNotNull(exceptionContext, "SubscriberExceptionContext cannot "
          + "be null");
    }

    private Throwable getThrowable() {
      return exception;
    }
  }

}
