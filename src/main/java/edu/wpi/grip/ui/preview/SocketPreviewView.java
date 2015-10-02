package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TitledPane;
import javafx.scene.text.TextAlignment;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that shows a preview of the current value of a socket.  This allows the user to have real-time
 * feedback on an algorithm.
 */
public abstract class SocketPreviewView<T> extends TitledPane {
    private final EventBus eventBus;
    private final Socket<T> socket;
    private final ObjectProperty<T> valueProperty;

    /**
     * @param eventBus The EventBus used by the application
     * @param socket   An output socket to preview
     */
    public SocketPreviewView(EventBus eventBus, Socket<T> socket) {
        checkNotNull(eventBus);
        checkNotNull(socket);
        checkArgument(socket.getDirection() == Socket.Direction.OUTPUT);

        this.eventBus = eventBus;
        this.socket = socket;
        this.valueProperty = new SimpleObjectProperty<>(this, "value", socket.getValue());

        this.eventBus.register(this);

        this.setText(this.socket.getSocketHint().getIdentifier());
        this.getStyleClass().add("socket-preview");
        this.setCollapsible(false);

        this.setTextAlignment(TextAlignment.CENTER);
    }

    public Socket<T> getSocket() {
        return this.socket;
    }

    /**
     * The value of the socket being previewed.  Subclasses bind this to some
     */
    public ObjectProperty<T> valueProperty() {
        return this.valueProperty;
    }

    @Subscribe
    public void onSocketChanged(SocketChangedEvent event) {
        if (event.getSocket() == this.socket) {
            this.valueProperty.set(this.socket.getValue());
        }
    }
}
