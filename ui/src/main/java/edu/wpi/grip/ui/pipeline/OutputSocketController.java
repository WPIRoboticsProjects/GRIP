package edu.wpi.grip.ui.pipeline;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.ui.Controller;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that renders an {@link OutputSocket} that is the output of a step.  It shows a label with the identifier
 * of the output, as well as a handle for connections to other sockets, and some buttons to configure what do to with
 * the output.
 */
@ParametrizedController(url = "OutputSocket.fxml")
public class OutputSocketController implements Controller {

    @FXML
    private HBox root;

    @FXML
    private Label identifier;

    @FXML
    private Label type;

    @FXML
    private ToggleButton preview;

    @FXML
    private StackPane handlePane;

    private SocketHandleView.Factory socketHandleFactory;

    /**
     * The "handle" is a simple shape next ot the socket identifier that shows whether or not there is a connection
     * going to or from the socket.  If there is such a connection, the ConnectionView is rendered as a curve going
     * from one handle to another.
     */
    private SocketHandleView handle;

    private final OutputSocket socket;

    public interface Factory {
        OutputSocketController create(OutputSocket socket);
    }

    @Inject
    OutputSocketController(SocketHandleView.Factory socketHandleFactory, @Assisted OutputSocket socket) {
        this.socketHandleFactory = checkNotNull(socketHandleFactory, "Socket Handle factory can not be null");
        this.socket = checkNotNull(socket, "The output socket can not be null");
    }

    @FXML
    @SuppressWarnings("unchecked")
    public void initialize() {
        this.handle = socketHandleFactory.create(this.socket);

        this.handlePane.getChildren().add(this.handle);

        // Show a button to choose if we want to preview the socket or not
        this.preview.setSelected(this.socket.isPreviewed());
        this.preview.selectedProperty().addListener(value -> this.socket.setPreviewed(this.preview.isSelected()));

        SocketHint<?> socketHint = this.socket.getSocketHint();

        // Set the label on the control based on the identifier from the socket hint
        this.identifier.setText(socketHint.getIdentifier());
        this.type.setText(this.socket.getSocketHint().getTypeLabel());
    }

    public Socket getSocket() {
        return this.socket;
    }

    public SocketHandleView getHandle() {
        return this.handle;
    }

    @VisibleForTesting
    ToggleButton previewButton() {
        return preview;
    }

    @Override
    public Node getRoot() {
        return root;
    }
}
