package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.scene.control.TextArea;

/**
 * A <code>SocketPreviewView</code> that previews sockets by simply calling their <code>toString()</code> method and
 * showing the result in a text area.
 */
public class TextAreaSocketPreviewView<T> extends SocketPreviewView<T> {

    private final TextArea text;

    /**
     * @param eventBus The EventBus used by the application
     * @param socket   An output socket to preview
     */
    public TextAreaSocketPreviewView(EventBus eventBus, OutputSocket<T> socket) {
        super(eventBus, socket);

        this.setStyle("-fx-pref-width: 20em;");

        this.text = new TextArea(socket.getValue().toString());
        text.setEditable(false);

        this.setContent(text);
    }

    @Subscribe
    public void onSocketChanged(SocketChangedEvent event) {
        final Socket socket = event.getSocket();

        if (socket == this.getSocket()) {
            this.text.setText(socket.getValue().toString());
        }
    }
}
