package edu.wpi.grip.ui.preview;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sun.javafx.application.PlatformImpl;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.SocketPreviewChangedEvent;
import edu.wpi.grip.core.events.StepMovedEvent;
import edu.wpi.grip.core.sockets.OutputSocket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;

/**
 * Controller for a container that automatically shows previews of all sockets marked as "previewed".
 *
 * @see OutputSocket#isPreviewed()
 */
@Singleton
public class PreviewsController {

    @FXML
    private HBox previewBox;

    @Inject
    private EventBus eventBus;
    @Inject
    private Pipeline pipeline;
    @Inject
    private SocketPreviewViewFactory previewViewFactory;

    /**
     * Any time a step is moved in the pipeline, we have to re-sort the previews
     */
    @Subscribe
    public synchronized void onStepMoved(StepMovedEvent event) {
        PlatformImpl.runAndWait(() -> sortPreviews(getPreviews()));
    }

    /**
     * This function is called when a preview button is pushed/triggered
     */
    @Subscribe
    public synchronized void onSocketPreviewChanged(SocketPreviewChangedEvent event) {
        final OutputSocket<?> socket = event.getSocket();

        // This needs to run right away to avoid synchronization problems, although in practice this method only runs
        // in the UI thread anyways (since it fires in response to a button press)
        PlatformImpl.runAndWait(() -> {
            final ObservableList<SocketPreviewView<?>> previews = getPreviews();
            if (socket.isPreviewed()) {
                // When a socket previewed, add a new view, then sort all of the views so they stay ordered
                previews.add(previewViewFactory.create(socket));
                sortPreviews(previews);
            } else {
                // When a socket is no longer marked as previewed, find and remove the view associated with it
                previews.stream()
                        .filter(view -> event.isRegarding(view.getSocket()))
                        .findFirst()
                        .ifPresent(preview -> {
                            previews.remove(preview);
                            eventBus.unregister(preview);
                        });
            }
        });
    }

    @SuppressWarnings("unchecked")
    private ObservableList<SocketPreviewView<?>> getPreviews() {
        return (ObservableList) previewBox.getChildren();
    }

    /**
     * This should be called to sort all of the previews in the preview box.
     *
     * @param previews All of the previews
     */
    private void sortPreviews(ObservableList<SocketPreviewView<?>> previews) {
        assert Platform.isFxApplicationThread() : "Must be run in JavaFX thread";
        // Take a snapshot of the sources and the steps in the current state so the comparison won't break.
        // If we don't take a copy of the lists before trying to sort them then any concurrent modification to the pipeline
        // could cause the sort result to be wrong or.
        final ImmutableList<Source> sources = pipeline.getSources();
        final ImmutableList<Step> steps = pipeline.getSteps();

        final Comparator<SocketPreviewView<?>> comparePreviews =
                Comparator.comparing(SocketPreviewView::getSocket,
                        (OutputSocket<?> a, OutputSocket<?> b) -> compareSockets(a, b, steps, sources));
        FXCollections.sort(previews, comparePreviews);
    }

    /**
     * Given two sockets, determine which comes first in the pipeline.  This is used to sort the previews.
     * This method is static so that it doesn't rely on any member variables in order to calculate the sort.
     *
     * @param a       The first socket in the comparison
     * @param b       The second socket in the comparison
     * @param steps   A snapshot of the steps. This should be exactly the same list for the entire sort
     * @param sources A snapshot of the sources. This should be exactly the same list for the entire sort.
     */
    private static int compareSockets(OutputSocket<?> a, OutputSocket<?> b, ImmutableList<Step> steps, ImmutableList<Source> sources) {
        if (a.getStep().isPresent() && b.getStep().isPresent()) {
            final Step stepA = a.getStep().get(), stepB = b.getStep().get();

            if (stepA == stepB) {
                // If both sockets are in the same step, order them based on which is first in the step
                return stepA.getOutputSockets().stream()
                        .filter(socket -> socket == a || socket == b)
                        .findFirst().get() == a ? -1 : 1;
            } else {
                // If both sockets are in different steps, order them based on which step is first
                return steps.stream()
                        .filter(step -> step == stepA || step == stepB)
                        .findFirst().get() == stepA ? -1 : 1;
            }
        }

        if (a.getSource().isPresent() && b.getSource().isPresent()) {
            final Source sourceA = a.getSource().get(), sourceB = b.getSource().get();

            if (sourceA == sourceB) {
                // If both sockets are in the same source, order them based on which is first in the source
                return sourceA.getOutputSockets().stream()
                        .filter(socket -> socket == a || socket == b)
                        .findFirst().get() == a ? -1 : 1;
            } else {
                // If both sockets are from sources, order them based on the order of the sources in the pipeline
                return sources.stream()
                        .filter(source -> source == sourceA || source == sourceB)
                        .findFirst().get() == sourceA ? -1 : 1;
            }
        }

        // Lastly, if one socket is from a step and the other is from a source, the source always comes first
        return b.getStep().isPresent() ? -1 : 1;
    }
}
