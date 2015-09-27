package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.Collections;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The bubble next to each socket that connections go in and out of
 * <p>
 * This class handles the drag-and-drop events that allow users to add and remove connections.
 */
public class SocketHandleView extends Button {

    private static final PseudoClass CONNECTING_PSEUDO_CLASS = PseudoClass.getPseudoClass("connecting");
    private static final PseudoClass CONNECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("connected");

    private static Optional<SocketControlView> draggingSocket = Optional.empty();

    final private SocketControlView controlView;
    final private EventBus eventBus;

    final private BooleanProperty connectingProperty = new SimpleBooleanProperty(this, "connecting", false);
    final private BooleanProperty connectedProperty = new SimpleBooleanProperty(this, "connected", false);

    public SocketHandleView(EventBus eventBus, SocketControlView controlView) {
        checkNotNull(eventBus);
        checkNotNull(controlView);

        this.controlView = controlView;
        this.eventBus = eventBus;

        this.getStyleClass().add("socket-handle");

        this.connectingProperty.addListener((observableValue, oldValue, isConnecting) ->
                this.pseudoClassStateChanged(CONNECTING_PSEUDO_CLASS, isConnecting));

        this.connectedProperty.addListener((observableValue, oldValue, isConnected) ->
                this.pseudoClassStateChanged(CONNECTED_PSEUDO_CLASS, isConnected));

        // When the user starts dragging a socket handle, start forming a connection.  This involves keeping a
        // reference to the SocketView that the drag started at.
        this.setOnDragDetected(mouseEvent -> {
            Dragboard db = this.startDragAndDrop(TransferMode.ANY);
            db.setContent(Collections.singletonMap(DataFormat.PLAIN_TEXT, "socket"));
            mouseEvent.consume();

            this.connectingProperty.set(true);
            draggingSocket = Optional.of(this.controlView);
        });

        // Remove the "connecting" property (which changes the appearance of the handle) when the user moves the cursor
        // out of the socket handle or completes the drag.  If the user moves the cursor away, it only disables the
        // connecting property if it's not the socket that the user started dragging from, since that one is supposed
        // to be dragged away.
        this.setOnDragExited(dragEvent ->
                draggingSocket.ifPresent(other -> this.connectingProperty.set(this.controlView == other)));
        this.setOnDragDone(dragEvent -> this.connectingProperty.set(false));

        // When the user drops the connection onto another socket, add a new connection.
        this.setOnDragDropped(dragEvent -> {
            draggingSocket.ifPresent(other -> {
                Socket inputSocket, outputSocket;

                // Check which socket was the input and which was the output.  The user can create a connection by
                // dragging either into the other.
                if (other.isInputSocket()) {
                    inputSocket = other.getSocket();
                    outputSocket = this.controlView.getSocket();
                } else {
                    inputSocket = this.controlView.getSocket();
                    outputSocket = other.getSocket();
                }

                // Add a new connection for the two sockets
                @SuppressWarnings("unchecked")
                Connection connection = new Connection(this.eventBus, outputSocket, inputSocket);
                eventBus.post(new ConnectionAddedEvent(connection));
            });
        });

        // Accept input sockets being dragged onto output sockets and vice versa.
        this.setOnDragOver(dragEvent -> {
            draggingSocket.ifPresent(other -> {
                if (other.isInputSocket() != this.controlView.isInputSocket()) {
                    dragEvent.acceptTransferModes(TransferMode.ANY);
                    this.connectingProperty.set(true);
                }
            });
        });
    }

    /**
     * @return The boolean property that indicates if this socket is currently in the middle of being connected to
     * another socket.  In other words, the user has clicked it, but has not clicked another socket yet.
     */
    public BooleanProperty connectingProperty() {
        return this.connectingProperty;
    }

    /**
     * @return The boolean property that indicates if this socket has at least one connection going in or out of it
     */
    public BooleanProperty connectedProperty() {
        return this.connectedProperty;
    }
}
