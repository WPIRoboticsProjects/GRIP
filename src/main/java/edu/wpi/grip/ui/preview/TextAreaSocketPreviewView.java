package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Socket;
import javafx.scene.control.TextArea;

/**
 * A <code>SocketPreviewView</code> that previews sockets by simply calling their <code>toString()</code> method and
 * showing the result in a text area.
 */
public class TextAreaSocketPreviewView<T> extends SocketPreviewView<T> {

    /**
     * @param eventBus The EventBus used by the application
     * @param socket   An output socket to preview
     */
    public TextAreaSocketPreviewView(EventBus eventBus, Socket<T> socket) {
        super(eventBus, socket);

        this.setStyle("-fx-pref-width: 20em;");

        final TextArea text = new TextArea(this.valueProperty().asString().get());
        text.setEditable(false);
        text.textProperty().bind(this.valueProperty().asString());

        this.setContent(text);
    }
}
