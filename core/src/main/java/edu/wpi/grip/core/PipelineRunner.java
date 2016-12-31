package edu.wpi.grip.core;


import edu.wpi.grip.core.events.BenchmarkEvent;
import edu.wpi.grip.core.events.RenderEvent;
import edu.wpi.grip.core.events.RunPipelineEvent;
import edu.wpi.grip.core.events.RunStartedEvent;
import edu.wpi.grip.core.events.RunStoppedEvent;
import edu.wpi.grip.core.events.StopPipelineEvent;
import edu.wpi.grip.core.metrics.Timer;
import edu.wpi.grip.core.util.SinglePermitSemaphore;
import edu.wpi.grip.core.util.service.AutoRestartingService;
import edu.wpi.grip.core.util.service.LoggingListener;
import edu.wpi.grip.core.util.service.RestartableService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Runs the pipeline in a separate thread. The runner listens for {@link RunPipelineEvent
 * RunPipelineEvents} and releases the pipeline thread to update the sources and run the steps.
 */
@Singleton
public class PipelineRunner implements RestartableService {
  private static final Logger logger = Logger.getLogger(PipelineRunner.class.getName());
  /**
   * This is used to flag that the pipeline needs to run because of an update.
   */
  private final SinglePermitSemaphore pipelineFlag = new SinglePermitSemaphore();
  private final Supplier<ImmutableList<Source>> sourceSupplier;
  private final Supplier<ImmutableList<Step>> stepSupplier;
  private final AutoRestartingService pipelineService;

  private final AtomicBoolean benchmarking = new AtomicBoolean(false);

  @Inject
  PipelineRunner(EventBus eventBus,
                 Provider<Pipeline> pipelineProvider,
                 Timer.Factory timerFactory) {
    this(eventBus,
        () -> pipelineProvider.get().getSources(),
        () -> pipelineProvider.get().getSteps(),
        timerFactory);
  }

  PipelineRunner(EventBus eventBus,
                 Supplier<ImmutableList<Source>> sourceSupplier,
                 Supplier<ImmutableList<Step>> stepSupplier,
                 Timer.Factory timerFactory) {
    this.sourceSupplier = sourceSupplier;
    this.stepSupplier = stepSupplier;
    Timer timer = timerFactory.create(this);
    this.pipelineService = new AutoRestartingService<>(
        () -> new AbstractScheduledService() {

          /**
           *
           * @throws InterruptedException This should never happen.
           */
          @Override
          protected void runOneIteration() throws InterruptedException {
            if (!super.isRunning()) {
              return;
            }

            pipelineFlag.acquire();
            eventBus.post(new RunStartedEvent());

            if (!super.isRunning()) {
              return;
            }
            timer.time(() -> runPipeline(super::isRunning));
            // This should not block access to the steps array
            eventBus.post(new RunStoppedEvent());
            if (super.isRunning()) {
              eventBus.post(new RenderEvent());
            }
          }

          @Override
          protected Scheduler scheduler() {
            return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.MILLISECONDS);
          }

          @Override
          protected String serviceName() {
            return "Pipeline Runner Service";
          }
        }
    );
    this.pipelineService.addListener(new LoggingListener(logger, PipelineRunner.class),
        MoreExecutors.directExecutor());
  }

  /**
   * Starts the pipeline to run at the default rate.
   */
  @Override
  public PipelineRunner startAsync() {
    pipelineService.startAsync();
    return this;
  }

  @Override
  public boolean isRunning() {
    return pipelineService.isRunning();
  }

  @Override
  public State state() {
    return pipelineService.state();
  }

  @Override
  public PipelineRunner stopAsync() {
    pipelineService.stopAsync();
    // Ensure that we unblock the pipeline so it can actually stop
    pipelineFlag.release();
    return this;
  }

  @Override
  public void stopAndAwait() {
    stopAsync().pipelineService.stopAndAwait();

  }

  @Override
  public void stopAndAwait(long timeout, TimeUnit unit) throws TimeoutException {
    stopAsync().pipelineService.stopAndAwait(timeout, unit);
  }

  @Override
  public void awaitRunning() {
    pipelineService.awaitRunning();
  }

  @Override
  public void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException {
    pipelineService.awaitRunning(timeout, unit);
  }

  @Override
  public void awaitTerminated() {
    pipelineService.awaitTerminated();
  }

  @Override
  public void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException {
    pipelineService.awaitTerminated(timeout, unit);
  }

  @Override
  public Throwable failureCause() {
    return pipelineService.failureCause();
  }

  @Override
  public void addListener(Listener listener, Executor executor) {
    pipelineService.addListener(listener, executor);
  }

  /**
   * This runs the pipeline immediately in the current thread.
   */
  @VisibleForTesting
  void runPipeline() {
    runPipeline(() -> true);
  }

  private void runPipeline(Supplier<Boolean> isRunning) {
    // Take a snapshot of both of the pipeline at the present time before running it.
    final ImmutableList<Source> sources = sourceSupplier.get();
    final ImmutableList<Step> steps = stepSupplier.get();
    // Now that we have a snapshot we can run the pipeline with our copy.

    if (!benchmarking.get()) {
      // Don't update sources if this run is being benchmarked
      for (Source source : sources) {
        // if we have been stopped then we need to exit as soon as possible.
        // then don't continue to run the pipeline.
        if (!isRunning.get()) {
          break;
        }
        source.updateOutputSockets();
      }
    }

    for (Step step : steps) {
      if (!isRunning.get()) {
        break;
      }
      step.runPerform(benchmarking.get());
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onRunPipeline(RunPipelineEvent event) {
    checkNotNull(event);
    if (event.pipelineShouldRun()) {
      pipelineFlag.release();
    }
  }

  @Subscribe
  public void onStopPipeline(@Nullable StopPipelineEvent event) {
    stopAsync();
  }

  @Subscribe
  public void onBenchmarkEvent(BenchmarkEvent event) {
    benchmarking.set(event.isStart());
  }

}
