package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.SocketPreviewChangedEvent;
import edu.wpi.grip.core.events.StepMovedEvent;
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
import java.util.Stack;

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
    private final PipelineView pipeline;//This is used to determine the order the previews are shown in.

    public PreviewsView(EventBus eventBus, PipelineView pipeline) {
        checkNotNull(eventBus);
        checkNotNull(pipeline);

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
     * This function is called when a step moves in the pipeline.
     */
    public synchronized void onPreviewOrderChanged(StepMovedEvent event) {
        Platform.runLater(() -> {//Run this function on the main gui thread
            final Step movedStep = event.getStep(); //The step whose position in the pipeline has changed
            final int distanceMoved = event.getDistance(); //The number of indices (positive or negative) the step has been moved by
            final int numberOfSourcePreviews = getNumbOfSourcePreviews();//The number of previews opened that are displaying sources (NOT steps)

            final OutputSocket<?>[] socketsMovedArray = movedStep.getOutputSockets();//Grab all the output sockets of the step that has moved

            //Find the rightmost and leftmost position in the previews of the previewed sockets of the step that has moved
            int rightmostIndex = 0; //Set to minimum possible value so that the first index will overwrite it
            int leftmostIndex = this.previewedSockets.size();//Set to maximum possible value so that the first index will overwrite it

            Stack<OutputSocket<?>> previewedMovedSockets = new Stack<OutputSocket<?>>();//This will hold the sockets of the step that was moved that are open for preview

            for (OutputSocket<?> i : socketsMovedArray) {
                if (this.previewedSockets.indexOf(i)!= -1){//If this socket is previewed
                    previewedMovedSockets.push(i);
                    if (rightmostIndex < this.previewedSockets.indexOf(i))
                        rightmostIndex = this.previewedSockets.indexOf(i);
                    if (leftmostIndex > this.previewedSockets.indexOf(i))
                        leftmostIndex = this.previewedSockets.indexOf(i);
                }
            }

            //Deal with each previewed socket from the step that was moved in turn
            while (previewedMovedSockets.size() != 0){ //While there are still sockets to deal with on the stack
                OutputSocket<?> current = previewedMovedSockets.pop();//Grab the top socket on the stack
                int oldIndex = this.previewedSockets.indexOf(current);//Get the index of this preview so we can remove the correct entry

                int newLocation = 0;//This will hold the new index in the list of previewed sockets for this socket

                if(distanceMoved<0) //If the step moved left....
                    newLocation = leftmostIndex + distanceMoved; //Calculate the new index from the leftmost previewed socket of this step
                else //The step must have moved right....
                    newLocation = rightmostIndex + distanceMoved;//So calculate the new index from the rightmost previewed socket of this step

                if (newLocation <numberOfSourcePreviews){//If the new calculated index would put it in the midst of source previews
                    newLocation = numberOfSourcePreviews;//Make the index the location of the first non-source preview
                }else{ //The new index is the current location of another step (NOT a source)

                    //So we need to make sure that we jump over groups of previews associated with the same step as a unit

                    int count = 0;

                    if(distanceMoved<0) {//If the step moved left....
                        OutputSocket<?>  nextSocketInDirection = this.previewedSockets.get(newLocation);
                        while ((nextSocketInDirection.getStep().isPresent())
                                && (nextSocketInDirection.getStep().get() == this.previewedSockets.get(newLocation).getStep().get())){
                                count++;
                                nextSocketInDirection = this.previewedSockets.get(newLocation-count);
                        }
                        newLocation = newLocation - (count-1);

                    }else {//The step must have moved right....
                        while ((newLocation+count < this.previewedSockets.size())
                                && (this.previewedSockets.get(newLocation+count).getStep().get() == this.previewedSockets.get(newLocation).getStep().get())) {
                            count++;
                        }
                        newLocation = newLocation + (count - 1);
                    }

                }

                //Remove this socket from the previews
                this.previewedSockets.remove(oldIndex);
                this.eventBus.unregister(this.previewBox.getChildren().remove(oldIndex));

                if (newLocation > this.previewedSockets.size())//If the new index is too big for the list of previews
                    newLocation = this.previewedSockets.size();//Make it so it will be added to the end of the list of previews

                this.previewedSockets.add(newLocation, current);//...use this index to add it to the correct location in the list of previews open
                this.previewBox.getChildren().add(newLocation, SocketPreviewViewFactory.createPreviewView(this.eventBus, current));//...and display it in the correct location in the list of previews open
            }
        });

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

                        //Find the appropriate index to add this preview with...
                        int indexInPreviews = getIndexInPreviewsOfAStepSocket(socket);

                        this.previewedSockets.add(indexInPreviews, socket);//...use this index to add it to the correct location in the list of previews open
                        this.previewBox.getChildren().add(indexInPreviews, SocketPreviewViewFactory.createPreviewView(this.eventBus, socket));//...and display it in the correct location in the list of previews open in the gui

                    } else {//This is a socket associated with a source and not a pipeline step...

                        //Find the appropriate index to add this preview with.
                        int indexInSourcePreviews = getIndexInPreviewsOfASourceSocket(socket);

                        this.previewedSockets.add(indexInSourcePreviews, socket);//Add the preview to the appropriate place in the list of previewed sockets
                        this.previewBox.getChildren().add(indexInSourcePreviews, SocketPreviewViewFactory.createPreviewView(this.eventBus, socket));//Display the preview in the appropriate place
                    }
                }
            } else {//The socket was already previewed, so the user must be requesting to not show this preview (remove both it and the corresponding control)

                int index = this.previewedSockets.indexOf(socket);//Get the index of this preview so we can remove the correct entry
                if (index != -1) {//False when the preview isn't currently displayed
                    this.previewedSockets.remove(index);
                    this.eventBus.unregister(this.previewBox.getChildren().remove(index));
                }
            }
        });
    }

    /**
     * Find the correct index in the displayed previews for a socket associated with a source (NOT a step socket)
     * by comparing the indices in the pipeline.
     * Called in {@link PreviewsView#onSocketPreviewChanged}
     *
     * @param socket An output socket associated with a source (NOT a step)
     * @return The correct index (an int) in the list of displayed previews for the given <code>socket</code>
     * @see PreviewsView#onSocketPreviewChanged(SocketPreviewChangedEvent)
     */
    private int getIndexInPreviewsOfASourceSocket(OutputSocket<?> socket) {
        final Source socketSource = socket.getSource().get();//The source socket associated with the socket whose preview has changed
        final SourceView sourceView = this.pipeline.findSourceView(socketSource);//The gui object that displays the socketSource
        int indexOfSource = this.pipeline.getSources().indexOf(sourceView); //The index of the source that has the socket in the pipeline

        //Start with the first socket in the list of previewed sockets
        int indexInSourcePreviews = 0;
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
     * Called in {@link PreviewsView#onSocketPreviewChanged}
     *
     * @param socket An output socket associated with a step (NOT a source)
     * @return The correct index in the list of displayed previews for the given <code>socket</code>
     * @see PreviewsView#onSocketPreviewChanged(SocketPreviewChangedEvent)
     */
    private int getIndexInPreviewsOfAStepSocket(OutputSocket<?> socket) {
        int numbOfSourcePreviews = getNumbOfSourcePreviews();//Count how many *source* previews (not *step* previews) are currently displayed

        final Step socketStep = socket.getStep().get();//The pipeline step associated with the socket whose preview has changed
        final StepView stepView = this.pipeline.findStepView(socketStep);//The gui object that displays the socketStep
        int indexOfStep = this.pipeline.getSteps().indexOf(stepView); //The index of the step that has the socket in the pipeline

        //Start at the first non-source socket in the list of previewed sockets
        int indexInPreviews = numbOfSourcePreviews;

        while ((this.previewedSockets.size() > indexInPreviews)//While there are sockets in the list of previewed sockets yet to be examined
                && ((this.pipeline.getSteps().indexOf(this.pipeline.findStepView(this.previewedSockets.get(indexInPreviews).getStep().get()))) < indexOfStep)) {//...AND the socket at this index in the list of displayed sockets has an index in the pipeline less than the socket passed in as "socket"
            indexInPreviews++;
        }
        return indexInPreviews;
    }

    /**
     * Counts how many source previews (NOT step previews) are currently displayed.
     * Called in {@link PreviewsView#getIndexInPreviewsOfAStepSocket}
     *
     * @return The number of source (NOT step) previews that are currently displayed
     * @see PreviewsView#getIndexInPreviewsOfAStepSocket(OutputSocket)
     */
    private int getNumbOfSourcePreviews() {
        //Start at the beginning of the list.
        int numbOfSourcePreviews = 0;
        while ((this.previewedSockets.size() > numbOfSourcePreviews) //While there are still previews to examine
                && (!this.previewedSockets.get(numbOfSourcePreviews).getStep().isPresent())) { //If this is a source...
            numbOfSourcePreviews++;
        }
        return numbOfSourcePreviews;
    }
}
