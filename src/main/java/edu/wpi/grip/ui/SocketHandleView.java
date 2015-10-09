package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.SocketConnectedChangedEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The bubble next to each socket that connections go in and out of
 * <p>
 * This class handles the drag-and-drop events that allow users to add and remove connections.
 */
public class SocketHandleView extends Button {

    private static final PseudoClass CONNECTING_PSEUDO_CLASS = PseudoClass.getPseudoClass("connecting");
    private static final PseudoClass CONNECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("connected");

    private static Optional<Socket> draggingSocket = Optional.empty();

    final private Socket socket;
    final private EventBus eventBus;

    final private BooleanProperty connectingProperty = new SimpleBooleanProperty(this, "connecting", false);
    final private BooleanProperty connectedProperty = new SimpleBooleanProperty(this, "connected", false);

    public SocketHandleView(EventBus eventBus, Socket socket) {
        checkNotNull(eventBus);
        checkNotNull(socket);

        this.socket = socket;
        this.eventBus = eventBus;

        this.getStyleClass().add("socket-handle");

        this.connectingProperty.addListener((observableValue, oldValue, isConnecting) ->
                this.pseudoClassStateChanged(CONNECTING_PSEUDO_CLASS, isConnecting));

        this.connectedProperty.addListener((observableValue, oldValue, isConnected) ->
                this.pseudoClassStateChanged(CONNECTED_PSEUDO_CLASS, isConnected));

        this.eventBus.register(this);

        // When the user clicks on a socket, remove any connections associated with that socket.
        this.setOnMouseClicked(mouseEvent -> {
            Set<Connection> connections = this.socket.getConnections();

            // Post a new ConnectionRemovedEvent for each connection
            connections.stream()
                    .map(ConnectionRemovedEvent::new)
                    .collect(Collectors.toList())
                    .forEach(this.eventBus::post);
        });

        // When the user starts dragging a socket handle, start forming a connection.  This involves keeping a
        // reference to the SocketView that the drag started at.
        this.setOnDragDetected(mouseEvent -> {
            Dragboard db = this.startDragAndDrop(TransferMode.ANY);
            db.setContent(Collections.singletonMap(DataFormat.PLAIN_TEXT, "socket"));
            mouseEvent.consume();

            this.connectingProperty.set(true);
            draggingSocket = Optional.of(this.socket);

            ((Node)mouseEvent.getSource()).setCursor(Cursor.CLOSED_HAND);
        });

        // Remove the "connecting" property (which changes the appearance of the handle) when the user moves the cursor
        // out of the socket handle or completes the drag.  If the user moves the cursor away, it only disables the
        // connecting property if it's not the socket that the user started dragging from, since that one is supposed
        // to be dragged away.
        this.setOnDragExited(dragEvent ->
                draggingSocket.ifPresent(other -> this.connectingProperty.set(this.socket == other)));
        this.setOnDragDone(dragEvent -> {
            this.connectingProperty.set(false);
            ((Node)dragEvent.getSource()).setCursor(Cursor.DEFAULT);
        });

        // When the user drops the connection onto another socket, add a new connection.
        this.setOnDragDropped(dragEvent -> {
            draggingSocket.ifPresent(other -> {
                InputSocket inputSocket;
                OutputSocket outputSocket;

                // Check which socket was the input and which was the output.  The user can create a connection by
                // dragging either into the other.
                switch (other.getDirection()) {
                    case INPUT:
                        inputSocket = (InputSocket)other;
                        assert this.socket.getDirection().equals(Socket.Direction.OUTPUT) : "The socket was not an Output";
                        outputSocket = (OutputSocket)this.socket;
                        break;
                    case OUTPUT:
                        assert this.socket.getDirection().equals(Socket.Direction.INPUT) : "The socket was not an Input";
                        inputSocket = (InputSocket)this.socket;
                        outputSocket = (OutputSocket)other;
                        break;
                    default:
                        throw new IllegalStateException("The Socket was a type that wasn't expected " + other.getDirection());
                }
                final Connection connection = new Connection(eventBus, outputSocket, inputSocket);
                eventBus.post(new ConnectionAddedEvent(connection));
            });
        });

        // Accept input sockets being dragged onto output sockets and vice versa.
        this.setOnDragOver(dragEvent -> {
            draggingSocket.ifPresent(other -> {
                if (other.getDirection() != this.socket.getDirection()) {
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

    @Subscribe
    public void onSocketConnectedChanged(SocketConnectedChangedEvent event) {
        if (event.getSocket() == this.socket) {
            // Set the handle as "selected" whenever there is at least one connection connected to it
            this.connectedProperty().set(!this.socket.getConnections().isEmpty());
        }
    }
}
