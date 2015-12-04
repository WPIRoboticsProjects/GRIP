package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.SocketPreviewChangedEvent;
import edu.wpi.grip.ui.pipeline.PipelineView;
import edu.wpi.grip.ui.pipeline.SourceView;
import edu.wpi.grip.ui.pipeline.StepView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple JavaFX container that automatically shows previews of all sockets marked as "previewed".
 *
 * @see OutputSocket#isPreviewed()
 */
public class PreviewsView extends VBox {

    @FXML
    private HBox previewBox;

    private final EventBus eventBus;
    private final List<OutputSocket<?>> previewedSockets;
    private PipelineView pipeline;//This is used to determine the order the previews are shown in.

    public PreviewsView(EventBus eventBus, PipelineView pipeline) {
        checkNotNull(eventBus);

        this.eventBus = eventBus;
        this.previewedSockets = new ArrayList<>();
        this.pipeline = pipeline;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Previews.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.eventBus.register(this);
    }

    @Subscribe
    /**
     * This function is called when a preview button is pushed/triggered
     */
    public synchronized void onSocketPreviewChanged(SocketPreviewChangedEvent event) {
        Platform.runLater(() -> {//Run this function on the main gui thread

            final OutputSocket<?> socket = event.getSocket(); //The socket whose preview has changed

            if (socket.isPreviewed()) {// If the socket was just set as previewed, add it to the list of previewed sockets and add a new view for it.

                if (!this.previewedSockets.contains(socket)) {//If the socket is not already previewed...

                    if (socket.getStep().isPresent()) { //If this is a socket associated with a pipeline step (IE NOT a source)....

                        int indexInPreviews = getIndexInPreviewsOfAStepSocket(socket);//Find the appropriate index to add this preview with.

                        this.previewedSockets.add(indexInPreviews, socket);//...use this index to add it to the correct location in the list of previews open
                        this.previewBox.getChildren().add(indexInPreviews, SocketPreviewViewFactory.createPreviewView(this.eventBus, socket));//...and display it in the correct location in the list of previews open in the gui

                    } else {//This is a socket associated with a source and not a pipeline step...

                        int indexInSourcePreviews = getIndexInPreviewsOfASourceSocket(socket);//Find the appropriate index to add this preview with.

                        this.previewedSockets.add(indexInSourcePreviews, socket);//Add the preview to the appropriate place in the list of previewed sockets
                        this.previewBox.getChildren().add(indexInSourcePreviews, SocketPreviewViewFactory.createPreviewView(this.eventBus, socket));//Display the preview in the appropriate place
                    }
                }
            } else {//The socket was already previewed, so the user must be requesting to not show this preview
                // If the socket was just set as not previewed, remove both it and the corresponding control
                int index = this.previewedSockets.indexOf(socket);//Get the index of this preview so we can remove the correct entry
                if (index != -1) {//this is false when the preview isn't currently displayed
                    this.previewedSockets.remove(index);
                    this.eventBus.unregister(this.previewBox.getChildren().remove(index));
                }
            }
        });
    }

    /**
     * Find the correct index in the displayed previews for a socket associated with a source (NOT a step socket)
     * by comparing the indices in the pipeline.
     * Called in PreviewsView::onSocketPreviewChanged(SocketPreviewChangedEvent)
     *
     * @param socket An output socket associated with a source (NOT a step)
     * @return The correct index (an int) in the list of displayed previews for the given <code>socket</code>
     * @see PreviewsView#onSocketPreviewChanged(SocketPreviewChangedEvent)
     */
    private int getIndexInPreviewsOfASourceSocket(OutputSocket<?> socket) {
        Source socketSource = socket.getSource().get();//The source socket associated with the socket whose preview has changed
        final SourceView sourceView = this.pipeline.findSourceView(socketSource);//The gui object that displays the socketSource
        int indexOfSource = this.pipeline.getSources().indexOf(sourceView); //The index of the source that has the socket in the pipeline

        int indexInSourcePreviews = 0;//Start with the first socket in the list of previewed sockets
        //Find the correct index in the displayed source previews by comparing the indices
        while (((this.previewedSockets.size() > indexInSourcePreviews)//If there are previews still to be examined AND
                && (this.previewedSockets.get(indexInSourcePreviews).getSource().isPresent()))//AND If the preview at this index is a source...
                && ((this.pipeline.getSources().indexOf(this.pipeline.findSourceView(this.previewedSockets.get(indexInSourcePreviews).getSource().get()))) < indexOfSource)) {//AND the preview at this index is a source with an index in the list of sources less than this source
            indexInSourcePreviews++;
        }
        return indexInSourcePreviews;
    }

    /**
     * Find the correct index in the displayed previews for a socket associated with a step (NOT a source socket)
     * by comparing the indices in the pipeline, starting with the first non-source preview displayed.
     * Called in PreviewsView::onSocketPreviewChanged(SocketPreviewChangedEvent)
     *
     * @param socket An output socket associated with a step (NOT a source)
     * @return The correct index in the list of displayed previews for the given <code>socket</code>
     * @see PreviewsView#onSocketPreviewChanged(SocketPreviewChangedEvent)
     */
    private int getIndexInPreviewsOfAStepSocket(OutputSocket<?> socket) {
        int numbOfSourcePreviews = getNumbOfSourcePreviews();//Count how many *source* previews (not *step* previews) are currently displayed

        Step socketStep = socket.getStep().get();//The pipeline step associated with the socket whose preview has changed
        final StepView stepView = this.pipeline.findStepView(socketStep);//The gui object that displays the socketStep
        int indexOfStep = this.pipeline.getSteps().indexOf(stepView); //The index of the step that has the socket in the pipeline

        int indexInPreviews = numbOfSourcePreviews;//Start at the first non-source socket in the list of previewed sockets

        while ((this.previewedSockets.size() > indexInPreviews)//While there are sockets in the list of previewed sockets yet to be examined
                && ((this.pipeline.getSteps().indexOf(this.pipeline.findStepView(this.previewedSockets.get(indexInPreviews).getStep().get()))) < indexOfStep)) {//...AND the socket at this index in the list of displayed sockets has an index in the pipeline less than the socket passed in as "socket"
            indexInPreviews++;
        }
        return indexInPreviews;
    }

    /**
     * Counts how many source previews (NOT step previews) are currently displayed.
     * Called in PreviewsView::getIndexInPreviewsOfAStepSocket(OutputSocket<?> socket)
     *
     * @return The number of source (NOT step) previews that are currently displayed
     * @see PreviewsView#getIndexInPreviewsOfAStepSocket(OutputSocket)
     */
    private int getNumbOfSourcePreviews() {
        int numbOfSourcePreviews = 0;
        while ((this.previewedSockets.size() > numbOfSourcePreviews) //While there are still previews to examine
                && (!this.previewedSockets.get(numbOfSourcePreviews).getStep().isPresent())) { //If this is a source...
            numbOfSourcePreviews++;
        }
        return numbOfSourcePreviews;
    }
}
