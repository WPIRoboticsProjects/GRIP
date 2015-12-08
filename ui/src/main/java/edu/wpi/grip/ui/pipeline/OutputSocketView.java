package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.SocketHint;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that renders an {@link OutputSocket} that is the output of a step.  It shows a label with the identifier
 * of the output, as well as a handle for connections to other sockets, and some buttons to configure what do to with
 * the output.
 */
public class OutputSocketView extends HBox implements Initializable {

    @FXML
    private Label identifier;

    @FXML
    private Label type;

    @FXML
    private ToggleButton preview;

    @FXML
    private StackPane handlePane;

    /**
     * The "handle" is a simple shape next ot the socket identifier that shows whether or not there is a connection
     * going to or from the socket.  If there is such a connection, the ConnectionView is rendered as a curve going
     * from one handle to another.
     */
    private SocketHandleView handle;

    private final EventBus eventBus;
    private final OutputSocket socket;

    public OutputSocketView(EventBus eventBus, OutputSocket<?> socket) {
        checkNotNull(eventBus);
        checkNotNull(socket);

        this.eventBus = eventBus;
        this.socket = socket;
        this.handle = new SocketHandleView(this.eventBus, this.socket);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("OutputSocket.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.handlePane.getChildren().add(this.handle);

        // Show a button to choose if we want to preview the socket or not
        this.preview.setSelected(this.socket.isPreviewed());
        this.preview.selectedProperty().addListener(value -> this.socket.setPreviewed(this.preview.isSelected()));

        this.eventBus.register(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SocketHint<?> socketHint = this.socket.getSocketHint();

        // Set the label on the control based on the identifier from the socket hint
        this.identifier.setText(socketHint.getIdentifier());
        this.type.setText(socketHint.getType().getSimpleName());
    }

    public Socket getSocket() {
        return this.socket;
    }

    public SocketHandleView getHandle() {
        return this.handle;
    }
}
