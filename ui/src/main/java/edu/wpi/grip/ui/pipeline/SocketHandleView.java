package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.SocketConnectedChangedEvent;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.ui.dragging.DragService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.TransferMode;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The bubble next to each socket that connections go in and out of
 * <p>
 * This class handles the drag-and-drop events that allow users to add and remove connections.
 */
public class SocketHandleView extends Button {

    private static final PseudoClass DISABLED_PSEUDO_CLASS = PseudoClass.getPseudoClass("disabled");
    private static final PseudoClass CONNECTING_PSEUDO_CLASS = PseudoClass.getPseudoClass("connecting");
    private static final PseudoClass CONNECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("connected");

    private final EventBus eventBus;
    private final Socket socket;

    /**
     * Provides a singleton object to assign the socket being dragged from during dragging to allow for a
     * connection to be made.
     */
    @Singleton
    protected static final class SocketDragService extends DragService<Socket> {
        public SocketDragService() {
            super("socket");
        }
    }

    final private BooleanProperty connectingProperty = new SimpleBooleanProperty(this, "connecting", false);
    final private BooleanProperty connectedProperty = new SimpleBooleanProperty(this, "connected", false);

    public interface Factory {
        SocketHandleView create(Socket socket);
    }

    @Inject
    SocketHandleView(EventBus eventBus,
                     Pipeline pipeline,
                     Connection.Factory<Object> connectionFactory,
                     SocketDragService socketDragService,
                     @Assisted Socket socket) {
        this.eventBus = eventBus;
        this.socket = socket;

        this.setTooltip(new Tooltip("Drag to connect"));

        this.getStyleClass().addAll("socket-handle", socket.getDirection().toString().toLowerCase());

        this.connectingProperty.addListener((observableValue, oldValue, isConnecting) ->
                this.pseudoClassStateChanged(CONNECTING_PSEUDO_CLASS, isConnecting));

        this.connectedProperty.addListener((observableValue, oldValue, isConnected) ->
                this.pseudoClassStateChanged(CONNECTED_PSEUDO_CLASS, isConnected));

        this.connectedProperty().set(!this.socket.getConnections().isEmpty());

        // When the user clicks on a socket, remove any connections associated with that socket.
        this.setOnMouseClicked(mouseEvent -> {
            Set<Connection> connections = this.socket.getConnections();

            // Post a new ConnectionRemovedEvent for each connection
            connections.stream()
                    .map(ConnectionRemovedEvent::new)
                    .collect(Collectors.toList())
                    .forEach(this.eventBus::post);
        });

        // When the user starts dragging a socket handle, starting forming a connection.  This involves keeping a
        // reference to the SocketView that the drag started at.
        this.setOnDragDetected(mouseEvent -> {
            this.connectingProperty.set(true);
            socketDragService.beginDrag(this.socket, this, "socket");
            mouseEvent.consume();
        });

        // Remove the "connecting" property (which changes the appearance of the handle) when the user moves the cursor
        // out of the socket handle or completes the drag.  If the user moves the cursor away, it only disables the
        // connecting property if it's not the socket that the user started dragging from, since that one is supposed
        // to be dragged away.
        this.setOnDragExited(dragEvent -> {
            socketDragService.getValue().ifPresent( other -> {
                this.connectingProperty.set(this.socket == other);
            });
        });

        this.setOnDragDone(dragEvent -> {
            this.connectingProperty.set(false);
            socketDragService.completeDrag();
        });

        // When the user drops the connection onto another socket, add a new connection.
        this.setOnDragDropped(dragEvent -> {
            socketDragService.getValue().ifPresent(other -> {
                InputSocket inputSocket;
                OutputSocket outputSocket;

                // Check which socket was the input and which was the output.  The user can create a connection by
                // dragging either into the other.
                switch (other.getDirection()) {
                    case INPUT:
                        inputSocket = (InputSocket) other;
                        assert this.socket.getDirection().equals(Socket.Direction.OUTPUT) : "The socket was not an Output";
                        outputSocket = (OutputSocket) this.socket;
                        break;
                    case OUTPUT:
                        assert this.socket.getDirection().equals(Socket.Direction.INPUT) : "The socket was not an Input";
                        inputSocket = (InputSocket) this.socket;
                        outputSocket = (OutputSocket) other;
                        break;
                    default:
                        throw new IllegalStateException("The Socket was a type that wasn't expected " + other.getDirection());
                }
                final Connection connection = connectionFactory.create(outputSocket, inputSocket);
                eventBus.post(new ConnectionAddedEvent(connection));
            });
        });

        // Accept a drag event if it's possible to connect the two sockets
        this.setOnDragOver(dragEvent -> {
            socketDragService.getValue().ifPresent(other -> {
                if (pipeline.canConnect(this.socket, other)) {
                    dragEvent.acceptTransferModes(TransferMode.ANY);
                    this.connectingProperty.set(true);
                }
            });
        });

        // While dragging, disable any socket that we can't drag onto, providing visual feedback to the user about
        // what can be connected.
        socketDragService.getDragProperty().addListener(observable -> {
            Optional<Socket> other = socketDragService.getValue();
            pseudoClassStateChanged(DISABLED_PSEUDO_CLASS, other.isPresent() && !pipeline.canConnect(socket, other.get()));
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
        if (this.socket.equals(event.getSocket())) {
            // Set the handle as "selected" whenever there is at least one connection connected to it
            this.connectedProperty().set(!this.socket.getConnections().isEmpty());
        }
    }
}
