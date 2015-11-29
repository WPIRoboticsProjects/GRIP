package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.SocketPreviewChangedEvent;
import edu.wpi.grip.core.events.StepMovedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;
import edu.wpi.grip.ui.MainWindowView;
import edu.wpi.grip.ui.pipeline.PipelineView;
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

        final OutputSocket<?> socket = event.getSocket(); //The socket whose preview has changed

        if (socket.isPreviewed()) {// If the socket was just set as previewed, add it to the list of previewed sockets and add a new view for it.

            if (!this.previewedSockets.contains(socket)) {//If the socket is not already previewed...

                if (socket.getStep().isPresent()){ //If this is a socket associated with a pipeline step (IE NOT a source)....

                    Step socketStep = socket.getStep().get();//The pipeline step associated with the socket whose preview has changed
                    final StepView stepView = this.pipeline.findStepView(socketStep);//The gui object that displays the socketStep
                    int indexOfStep = this.pipeline.getSteps().indexOf(stepView); //The index of the step that has the socket in the pipeline

                    int numbOfSourcePreviews = 0;//This we will use to count how many *source* previews (not *step* previews) are currently displayed
                    while((this.previewedSockets.size()>numbOfSourcePreviews) //While there are still previews to examine
                            && ((this.previewedSockets.get(numbOfSourcePreviews).getSocketHint().getIdentifier().contains("Image")) //If this is a source (currently, sources can only have two types of output sockets, "Image" and "Frame Rate")...
                               ||(this.previewedSockets.get(numbOfSourcePreviews).getSocketHint().getIdentifier().contains("Frame Rate"))))
                        numbOfSourcePreviews++;

                    indexOfStep += numbOfSourcePreviews;//Add the number of source previews currently displayed to the index so that the source previews are always displayed first

                    final int indexFinal = indexOfStep;

                    if (indexFinal > this.previewBox.getChildren().size()) {//If the index is greater than the number of previews currently displayed...
                        this.previewedSockets.add(socket);//...then just add it to the end of the list of previews
                        this.previewBox.getChildren().add(SocketPreviewViewFactory.createPreviewView(this.eventBus, socket));//...and display it last in the preview view
                    } else { // If the index is <= the number of previews currently displayed...
                        this.previewedSockets.add(indexFinal, socket);//...use this index to add it to the correct location in the list of previews open
                        this.previewBox.getChildren().add(indexFinal, SocketPreviewViewFactory.createPreviewView(this.eventBus, socket));//...and display it in the correct location in the list of previews open in the gui
                    }
                }else{//This is a socket associated with a source and not a pipeline step...
                    this.previewedSockets.add(0,socket);//...so add it to the beginning of the list
                    this.previewBox.getChildren().add(0,SocketPreviewViewFactory.createPreviewView(this.eventBus, socket));//...and display it first in the preview gui window
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
    }
}
