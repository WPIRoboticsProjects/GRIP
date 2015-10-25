package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.ui.pipeline.SocketHandleView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX input that renders an {@link InputSocket} that is an input to a step.  This includes an identifier, a handle
 * for connections, and an optional input (if a view is specified in the socket hint) that lets the user manually
 * change the parameters of a step.
 * <p>
 * Subclasses of {@code InputSocketView} control what sort of input is used (for example, a slider or a checkbox)
 */
public abstract class InputSocketView<T> extends GridPane {

    @FXML
    private Label identifier;

    @FXML
    private StackPane contentPane;

    private ObjectProperty<Node> contentProperty;

    /**
     * The "handle" is a simple shape next ot the socket identifier that shows whether or not there is a connection
     * going to or from the socket.  If there is such a connection, the ConnectionView is rendered as a curve going
     * from one handle to another.
     */
    private SocketHandleView handle;

    private final EventBus eventBus;
    private final InputSocket<T> socket;

    protected InputSocketView(EventBus eventBus, InputSocket<T> socket) {
        checkNotNull(eventBus);
        checkNotNull(socket);

        this.eventBus = eventBus;
        this.socket = socket;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("InputSocket.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.identifier.setText(socket.getSocketHint().getIdentifier());

        this.handle = new SocketHandleView(this.eventBus, this.socket);
        this.add(this.handle, 0, 0);

        this.eventBus.register(this);
    }

    public Socket<T> getSocket() {
        return this.socket;
    }

    public SocketHandleView getHandle() {
        return this.handle;
    }

    public Node getContent() {
        return this.contentProperty().get();
    }

    public ObjectProperty<Node> contentProperty() {
        if (this.contentProperty == null) {
            this.contentProperty = new SimpleObjectProperty<>(this, "content");
            this.contentProperty.addListener(o -> this.contentPane.getChildren().setAll(this.getContent()));
        }

        return this.contentProperty;
    }

    public void setContent(Node node) {
        this.contentProperty().set(node);
    }

    public Label getIdentifier() {
        return this.identifier;
    }
}
