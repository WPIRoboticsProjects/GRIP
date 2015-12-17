package edu.wpi.grip.ui.util;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @see <a href="http://news.kynosarges.org/2014/05/01/simulating-platform-runandwait/">Source</a>
 */
public class GRIPPlatform {

    /**
     * Runs the specified {@link Runnable} on the
     * JavaFX application thread and waits for completion.
     *
     * @param action the {@link Runnable} to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runAndWait(Runnable action) {
        checkNotNull(action, "The action can not be null");

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                doneLatch.countDown();
            }
        });

        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            // ignore exception
        }
    }
}
