package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.Subscribe;
import com.sun.javafx.application.PlatformImpl;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.SocketPreviewChangedEvent;
import edu.wpi.grip.core.events.StepMovedEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Controller for a container that automatically shows previews of all sockets marked as "previewed".
 *
 * @see OutputSocket#isPreviewed()
 */
@Singleton
public class PreviewsController {

    @FXML private HBox previewBox;

    @Inject private Pipeline pipeline;
    @Inject private SocketPreviewViewFactory previewViewFactory;

    private final Comparator<SocketPreviewView<?>> comparePreviews =
            Comparator.comparing(SocketPreviewView::getSocket, this::compareSockets);

    /**
     * Any time a step is moved in the pipeline, we have to re-sort the previews
     */
    @Subscribe
    public synchronized void onStepMoved(StepMovedEvent event) {
        PlatformImpl.runAndWait(() -> FXCollections.sort((ObservableList) previewBox.getChildren(), comparePreviews));
    }

    /**
     * This function is called when a preview button is pushed/triggered
     */
    @Subscribe
    public synchronized void onSocketPreviewChanged(SocketPreviewChangedEvent event) {
        OutputSocket<?> socket = event.getSocket();

        @SuppressWarnings("unchecked")
        ObservableList<SocketPreviewView<?>> previews = (ObservableList) previewBox.getChildren();

        // This needs to run right away to avoid synchronization problems, although in practice this method only runs
        // in the UI thread anyways (since it fires in response to a button press)
        PlatformImpl.runAndWait(() -> {
            if (socket.isPreviewed()) {
                // When a socket previewed, add a new view, then sort all of the views so they stay ordered
                previews.add(previewViewFactory.create(socket));
                FXCollections.sort(previews, comparePreviews);
            } else {
                // When a socket is no longer marked as previewed, find and remove the view associated with it
                previews.stream()
                        .filter(view -> view.getSocket() == socket)
                        .findFirst()
                        .ifPresent(previews::remove);
            }
        });
    }

    /**
     * Given two sockets, determine which comes first in the pipeline.  This is used to sort the previews.
     */
    private int compareSockets(OutputSocket<?> a, OutputSocket<?> b) {
        if (a.getStep().isPresent() && b.getStep().isPresent()) {
            final Step stepA = a.getStep().get(), stepB = b.getStep().get();

            if (stepA == stepB) {
                // If both sockets are in the same step, order them based on which is first in the step
                return Arrays.asList(stepA.getOutputSockets()).stream()
                        .filter(socket -> socket == a || socket == b)
                        .findFirst().get() == a ? -1 : 1;
            } else {
                // If both sockets are in different steps, order them based on which step is first
                return pipeline.getSteps().stream()
                        .filter(step -> step == stepA || step == stepB)
                        .findFirst().get() == stepA ? -1 : 1;
            }
        }

        if (a.getSource().isPresent() && b.getSource().isPresent()) {
            final Source sourceA = a.getSource().get(), sourceB = b.getSource().get();

            if (sourceA == sourceB) {
                // If both sockets are in the same source, order them based on which is first in the source
                return Arrays.asList(sourceA.getOutputSockets()).stream()
                        .filter(socket -> socket == a || socket == b)
                        .findFirst().get() == a ? -1 : 1;
            } else {
                // If both sockets are from sources, order them based on the order of the sources in the pipeline
                return pipeline.getSources().stream()
                        .filter(source -> source == sourceA || source == sourceB)
                        .findFirst().get() == sourceA ? -1 : 1;
            }
        }

        // Lastly, if one socket is from a step and the other is from a source, the source always comes first
        return b.getStep().isPresent() ? -1 : 1;
    }
}
