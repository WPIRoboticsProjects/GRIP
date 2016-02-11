package edu.wpi.grip.ui.util;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @see <a href="http://news.kynosarges.org/2014/05/01/simulating-platform-runandwait/">Source</a>
 */
@Singleton
public class GRIPPlatform {

    private final EventBus eventBus;
    private final Logger logger;

    private static class JavaFXRunnerEvent {
        private final Runnable action;

        public JavaFXRunnerEvent(Runnable action) {
            this.action = checkNotNull(action, "Action can not be null");
        }

        public Runnable getAction() {
            return action;
        }
    }

    @Inject
    GRIPPlatform(EventBus eventBus, Logger logger) {
        checkArgument(!(eventBus instanceof AsyncEventBus), "This class has not been tested to work with the AsyncEventBus");
        this.eventBus = eventBus;
        this.logger = logger;
    }


    /**
     * Runs the specified {@link Runnable} on the
     * JavaFX application thread. If we are already on the JavaFX application thread
     * then this will be run immediately. Otherwise, it will be run as an event
     * inside of the event bus.
     * If
     * If {@link #runAsSoonAsPossible(Runnable)} is called within itself it will always run
     * immediately because the runnable will always be run in the JavaFX thread.
     *
     * @param action the {@link Runnable} to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public void runAsSoonAsPossible(Runnable action) {
        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            // Do not move this into the subscriber.
            // It will cause a deadlock in the event subscriber.
            action.run();
            return;
        } else {
            eventBus.post(new JavaFXRunnerEvent(action));
        }

    }

    @Subscribe
    public void onJavaFXRunnerEvent(JavaFXRunnerEvent event) throws InterruptedException {
        assert !Platform.isFxApplicationThread() : "This should never be run on the application thread. This can cause a deadlock!";
        final Thread callingThread = Thread.currentThread();
        if (Thread.interrupted()) {
            throw new InterruptedException("Interrupted in onJavaFXRunnerEvent");
        }

        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // If the calling thread was interrupted then don't run this event.
                if(!callingThread.isInterrupted()) {
                    event.getAction().run();
                }
            } finally {
                doneLatch.countDown();
            }
        });

        while (!doneLatch.await(500, TimeUnit.MILLISECONDS)) {
            logger.log(Level.WARNING, "POTENTIAL DEADLOCK!");
        }
    }

}
