package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import javafx.scene.control.TitledPane;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that shows a preview of the current value of a socket.  This allows the user to have real-time
 * feedback on an algorithm.
 */
public abstract class SocketPreviewView<T> extends TitledPane {
    private final EventBus eventBus;
    private final OutputSocket<T> socket;

    /**
     * @param eventBus The EventBus used by the application
     * @param socket   An output socket to preview
     */
    public SocketPreviewView(EventBus eventBus, OutputSocket<T> socket) {
        checkNotNull(eventBus);
        checkNotNull(socket);

        this.eventBus = eventBus;
        this.socket = socket;

        this.eventBus.register(this);

        this.setText(this.socket.getSocketHint().getIdentifier());
        this.getStyleClass().add("socket-preview");
        this.setCollapsible(false);
    }

    public OutputSocket<T> getSocket() {
        return this.socket;
    }
}
