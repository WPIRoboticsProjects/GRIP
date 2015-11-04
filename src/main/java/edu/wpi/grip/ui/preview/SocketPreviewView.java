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

        this.setText(this.getTitle());
        this.getStyleClass().add("socket-preview");
        this.setCollapsible(false);
    }

    /**
     * @return A string of text to display in the GUI to identify this preview, including the name of the output socket
     * and what operation it is the output of.
     */
    private String getTitle() {
        String title = "";

        if (this.socket.getStep().isPresent()) {
            title += this.socket.getStep().get().getOperation().getName() + " → ";
        }

        title += this.socket.getSocketHint().getIdentifier();

        return title;
    }

    public OutputSocket<T> getSocket() {
        return this.socket;
    }
}
