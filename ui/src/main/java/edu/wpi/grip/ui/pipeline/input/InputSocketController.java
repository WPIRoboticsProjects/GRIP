package edu.wpi.grip.ui.pipeline.input;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.ui.Controller;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.pipeline.SocketHandleView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX input that renders an {@link InputSocket} that is an input to a step.  This includes an identifier, a handle
 * for connections, and an optional input (if a view is specified in the socket hint) that lets the user manually
 * change the parameters of a step.
 * <p>
 * Subclasses of {@code InputSocketController} control what sort of input is used (for example, a slider or a checkbox)
 */
@ParametrizedController(url = "InputSocket.fxml")
public class InputSocketController<T> implements Controller {

    @FXML
    private GridPane root;

    @FXML
    private Label identifier;

    @FXML
    private Label type;

    @FXML
    private StackPane contentPane;

    private ObjectProperty<Node> contentProperty;

    /**
     * The "handle" is a simple shape next ot the socket identifier that shows whether or not there is a connection
     * going to or from the socket.  If there is such a connection, the ConnectionView is rendered as a curve going
     * from one handle to another.
     */
    private SocketHandleView handle;

    private final SocketHandleView.Factory socketHandleViewFactory;
    private final InputSocket<T> socket;

    public interface BaseInputSocketControllerFactory<T> {
        InputSocketController<T> create(InputSocket<T> socket);
    }

    @Inject
    InputSocketController(SocketHandleView.Factory socketHandleViewFactory, @Assisted InputSocket<T> socket) {
        this.socketHandleViewFactory = socketHandleViewFactory;
        this.socket = checkNotNull(socket);
    }

    @FXML
    public void initialize() {
        this.identifier.setText(this.socket.getSocketHint().getIdentifier());
        this.type.setText(this.socket.getSocketHint().getTypeLabel());
        this.handle = socketHandleViewFactory.create(this.socket);
        root.add(this.handle, 0, 0);
    }

    public Socket<T> getSocket() {
        return this.socket;
    }

    public SocketHandleView getHandle() {
        if (this.handle == null) {
            throw new IllegalStateException("Get Handle can only be called after the FXML has been initialized!");
        }
        return this.handle;
    }

    public Node getContent() {
        return this.contentProperty().get();
    }

    public ObjectProperty<Node> contentProperty() {
        if (this.contentPane == null) {
            throw new IllegalStateException("contentProperty can only be called after the FXML has been initialized!");
        }
        if (this.contentProperty == null) {
            this.contentProperty = new SimpleObjectProperty<>(this, "content");
            this.contentProperty.addListener(o -> this.contentPane.getChildren().setAll(this.getContent()));
        }

        return this.contentProperty;
    }

    protected void setContent(Node node) {
        this.contentProperty().set(node);
    }

    protected Label getIdentifier() {
        if (this.identifier == null) {
            throw new IllegalStateException("getIdentifier can only be called after the FXML has been initialized!");
        }
        return this.identifier;
    }

    public final GridPane getRoot() {
        if (this.root == null) {
            throw new IllegalStateException("getIdentifier can only be called after the FXML has been initialized!");
        }
        return root;
    }
}
