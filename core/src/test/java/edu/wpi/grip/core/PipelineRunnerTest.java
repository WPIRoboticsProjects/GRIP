package edu.wpi.grip.core;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import edu.wpi.grip.core.events.ExceptionEvent;
import edu.wpi.grip.core.events.RenderEvent;
import edu.wpi.grip.core.events.RunPipelineEvent;
import edu.wpi.grip.core.events.StopPipelineEvent;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.MockInputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.util.MockExceptionWitness;
import net.jodah.concurrentunit.Waiter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(Enclosed.class)
public class PipelineRunnerTest {

    public static class SimpleTests {
        private FailureListener failureListener;

        @Before
        public void setUp() {
            failureListener = new FailureListener();
        }

        @After
        public void tearDown() throws Throwable {
            failureListener.throwIfProblemPresent();
        }

        @Test
        public void testRunEmptyPipelineSucceeds() {
            PipelineRunner runner = new PipelineRunner(null, () -> ImmutableList.of(), () -> ImmutableList.of());
            runner.addListener(failureListener, MoreExecutors.directExecutor());
            runner.startAsync().awaitRunning();
            assertEquals("Runner should be running", Service.State.RUNNING, runner.state());

            runner.stopAndAwait();
            assertEquals("Runner should have stopped in a timely manner", Service.State.TERMINATED, runner.state());
        }

        @Test
        public void testRunSimplePipeline_WithSourcesAndSteps() throws IOException {
            final EventBus eventBus = new EventBus();
            final MockSource source = new MockSource();
            final MockStep step = new MockStep();

            final PipelineRunner runner = new PipelineRunner(eventBus, () -> ImmutableList.of(source), () -> ImmutableList.of(step));
            runner.addListener(failureListener, MoreExecutors.directExecutor());
            runner.startAsync().awaitRunning();
            assertEquals("Runner should be running", Service.State.RUNNING, runner.state());

            runner.stopAndAwait();
            assertEquals("Runner should have stopped in a timely manner", Service.State.TERMINATED, runner.state());
        }

        @Test
        public void testStopPipelineEventStopsPipeline() throws TimeoutException {
            PipelineRunner runner = new PipelineRunner(null, () -> ImmutableList.of(), () -> ImmutableList.of());
            runner.addListener(failureListener, MoreExecutors.directExecutor());
            runner.startAsync().awaitRunning(3, TimeUnit.SECONDS);
            assertEquals("Runner should be running", Service.State.RUNNING, runner.state());

            runner.onStopPipeline(new StopPipelineEvent());
            runner.awaitTerminated(4, TimeUnit.SECONDS);
            assertEquals("Runner should have stopped in a timely manner", Service.State.TERMINATED, runner.state());
        }

        @Test
        public void testRunningOperationThatThrowsExceptionWillNotPropagate() throws TimeoutException {
            final EventBus eventBus = new EventBus();
            final Waiter renderWaiter = new Waiter();
            final String illegalAugmentExceptionMessage = "Kersplat!";
            class OperationThatThrowsExceptionOnPerform implements SimpleOperation {
                @Override
                public void perform() {
                    throw new IllegalArgumentException(illegalAugmentExceptionMessage);
                }
            }
            final Operation operation = new OperationThatThrowsExceptionOnPerform();
            final OperationMetaData operationMetaData = new OperationMetaData(OperationDescription.builder().name("OperationThatThrowsExceptionOnPerform").build(), () -> operation);
            class ExceptionEventReceiver {
                private int callCount = 0;
                private ExceptionEvent event;

                @Subscribe
                public void onException(ExceptionEvent event) {
                    this.event = event;
                    callCount++;
                }
            }
            final ExceptionEventReceiver exceptionEventReceiver = new ExceptionEventReceiver();
            eventBus.register(exceptionEventReceiver);
            eventBus.register(new RenderWaiterResumer(renderWaiter));

            final Step throwingStep = new Step.Factory(MockExceptionWitness.simpleFactory(eventBus)).create(operationMetaData);
            final PipelineRunner runner = new PipelineRunner(eventBus, () -> ImmutableList.of(), () -> ImmutableList.of(throwingStep));
            runner.addListener(failureListener, MoreExecutors.directExecutor());

            runner.startAsync().awaitRunning();
            assertEquals("Exception event should not have run", 0, exceptionEventReceiver.callCount);
            assertNull("Event should be null", exceptionEventReceiver.event);

            runner.onRunPipeline(new RunPipelineEvent() {
            });

            renderWaiter.await(3, TimeUnit.SECONDS);

            runner.stopAndAwait(3, TimeUnit.SECONDS);

            assertEquals("Exception event should have only run once", 1, exceptionEventReceiver.callCount);
            assertTrue("Exception event should have an exception", exceptionEventReceiver.event.getException().isPresent());
            assertThat(exceptionEventReceiver.event.getException().get()).isInstanceOf(IllegalArgumentException.class);
            assertThat(exceptionEventReceiver.event.getException().get()).hasMessage(illegalAugmentExceptionMessage);

        }
    }


    public static class RunnerCounterTests {
        private EventBus eventBus;
        private Waiter renderWaiter;
        private RunSourceCounter sourceCounter;
        private RunCounterOperation operationCounter;
        private Step runCounterStep;
        private FailureListener failureListener;

        @Before
        public void setUp() {
            eventBus = new EventBus();
            renderWaiter = new Waiter();
            sourceCounter = new RunSourceCounter();
            operationCounter = new RunCounterOperation();
            runCounterStep = new Step.Factory(MockExceptionWitness.MOCK_FACTORY).create(new OperationMetaData(RunCounterOperation.DESCRIPTION, () -> operationCounter));
            failureListener = new FailureListener();

        }

        @After
        public void tearDown() throws Throwable {
            failureListener.throwIfProblemPresent();
        }

        @Test
        @SuppressWarnings("PMD.AvoidDuplicateLiterals")
        public void testOperationNormalMethodCallCount() throws TimeoutException {
            eventBus.register(new RenderWaiterResumer(renderWaiter));
            final PipelineRunner runner = new PipelineRunner(eventBus, () -> ImmutableList.of(sourceCounter), () -> ImmutableList.of(runCounterStep));
            runner.addListener(failureListener, MoreExecutors.directExecutor());

            runner.startAsync().awaitRunning();
            assertEquals("Update should not have run", 0, sourceCounter.updateCount);
            assertEquals("Perform should not have run", 0, operationCounter.performCount);
            assertEquals("CleanUp should not have run", 0, operationCounter.cleanUpCount);

            runner.onRunPipeline(new RunPipelineEvent() {
                @Override
                public boolean pipelineShouldRun() {
                    return false;
                }
            });
            assertTrue("Runner should not have stopped running", runner.isRunning());

            assertEquals("Update should not have run", 0, sourceCounter.updateCount);
            assertEquals("Perform should not have run", 0, operationCounter.performCount);
            assertEquals("CleanUp should not have run", 0, operationCounter.cleanUpCount);

            runner.onRunPipeline(new RunPipelineEvent() {
                @Override
                public boolean pipelineShouldRun() {
                    return true;
                }
            });

            renderWaiter.await(10, TimeUnit.SECONDS);

            // If this fails: Did you move where the render event gets posted?
            assertEquals("Update should not have run", 1, sourceCounter.updateCount);
            assertEquals("Perform have run", 1, operationCounter.performCount);
            assertEquals("CleanUp should not have run", 0, operationCounter.cleanUpCount);

            runner.stopAndAwait();
        }

        @Test
        @SuppressWarnings("PMD.AvoidDuplicateLiterals")
        public void testRemovedStepWillNotRun() {
            final PipelineRunner runner = new PipelineRunner(eventBus,
                    () -> ImmutableList.of(),
                    () -> ImmutableList.of(runCounterStep));
            runner.addListener(failureListener, MoreExecutors.directExecutor());

            runner.startAsync().awaitRunning();
            assertEquals("Perform should not have run", 0, operationCounter.performCount);
            assertEquals("CleanUp should not have run", 0, operationCounter.cleanUpCount);

            runCounterStep.setRemoved();

            assertEquals("Perform should not have run", 0, operationCounter.performCount);
            assertEquals("CleanUp ran an unexpected number of times", 1, operationCounter.cleanUpCount);

            runner.onRunPipeline(new RunPipelineEvent() {
                // Defaults to true
            });

            assertEquals("Perform should not have run", 0, operationCounter.performCount);
            assertEquals("CleanUp ran an unexpected number of times", 1, operationCounter.cleanUpCount);
            runner.stopAndAwait();

            assertEquals("Perform should not have run", 0, operationCounter.performCount);
            assertEquals("CleanUp ran an unexpected number of times", 1, operationCounter.cleanUpCount);
        }

        @Test
        public void testPipelineWontRunOperationIfStoppedAfterRunPipelineEvent() throws TimeoutException {
            final Waiter sourceSupplierWaiter = new Waiter();
            final Waiter supplierBlockedWaiter = new Waiter();
            final PipelineRunner runner = new PipelineRunner(eventBus, () -> {
                try {
                    supplierBlockedWaiter.resume();
                    sourceSupplierWaiter.await();
                } catch (TimeoutException e) {
                    throw new IllegalStateException(e);
                }
                return ImmutableList.of();
            }, () -> ImmutableList.of(runCounterStep));
            runner.addListener(failureListener, MoreExecutors.directExecutor());

            runner.startAsync().awaitRunning(3, TimeUnit.SECONDS);

            runner.onRunPipeline(new RunPipelineEvent() {
            });

            supplierBlockedWaiter.await();
            runner.stopAsync();
            sourceSupplierWaiter.resume();

            runner.awaitTerminated(3, TimeUnit.SECONDS);
            assertEquals("Perform should not have run", 0, operationCounter.performCount);
            assertEquals("CleanUp should not have run", 0, operationCounter.cleanUpCount);
        }

        @Test
        public void testPipelineWontRunSourceIfStoppedAfterRunPipelineEvent() throws TimeoutException {
            final Waiter sourceSupplierWaiter = new Waiter();
            final Waiter supplierBlockedWaiter = new Waiter();
            final PipelineRunner runner = new PipelineRunner(eventBus, () -> {
                try {
                    supplierBlockedWaiter.resume();
                    sourceSupplierWaiter.await();
                } catch (TimeoutException e) {
                    throw new IllegalStateException(e);
                }
                return ImmutableList.of(sourceCounter);
            }, () -> ImmutableList.of());
            runner.addListener(failureListener, MoreExecutors.directExecutor());

            runner.startAsync().awaitRunning(3, TimeUnit.SECONDS);

            runner.onRunPipeline(new RunPipelineEvent() {
            });

            supplierBlockedWaiter.await();
            runner.stopAsync();
            sourceSupplierWaiter.resume();

            runner.awaitTerminated(3, TimeUnit.SECONDS);
            assertEquals("Source Update should not have run", 0, sourceCounter.updateCount);
        }

    }

    static class RenderWaiterResumer {
        private final Waiter waiter;

        private RenderWaiterResumer(Waiter waiter) {
            this.waiter = waiter;
        }

        @Subscribe
        public void onRenderEvent(RenderEvent event) {
            waiter.resume();
        }
    }

    static class RunSourceCounter extends MockSource {
        private int updateCount = 0;

        @Override
        protected boolean updateOutputSockets() {
            updateCount++;
            return false;
        }
    }

    static class RunCounterOperation implements SimpleOperation {
        private static final OperationDescription DESCRIPTION = OperationDescription.builder().name("Simple").build();
        private int performCount = 0;
        private int cleanUpCount = 0;

        @Override
        public void perform() {
            performCount++;
        }

        @Override
        public void cleanUp() {
            cleanUpCount++;
        }
    }

    interface SimpleOperation extends Operation {
        OperationDescription DESCRIPTION = OperationDescription.builder()
                .name("Simple Operation")
                .summary("A simple operation for testing")
                .build();

        @Override
        default List<InputSocket> getInputSockets() {
            return ImmutableList.of(
                    new MockInputSocket("Test Socket") {
                        @Override
                        public boolean dirtied() {
                            return true;
                        }
                    }
            );
        }


        @Override
        default List<OutputSocket> getOutputSockets() {
            return ImmutableList.of();
        }
    }

    static class FailureListener extends Service.Listener {
        private Service.State failedFrom = null;
        private Throwable failure = null;

        public synchronized void failed(Service.State from, Throwable failure) {
            this.failedFrom = from;
            this.failure = failure;
        }

        @SuppressWarnings("PMD.SystemPrintln")
        public synchronized void throwIfProblemPresent() throws Throwable {
            if (failedFrom != null || failure != null) {
                System.err.println("Failed from state " + failedFrom);
                throw failure;
            }
        }
    }
}
