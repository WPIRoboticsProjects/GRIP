package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Socket;

/**
 * Factory for constructing {@link SocketPreviewView}s
 */
public class SocketPreviewViewFactory {

    /**
     * Create an instance of {@link SocketPreviewView} appropriate for the given socket.  Sockets of different types
     * (like numbers, arrays, images, etc...) are rendered in different ways, and the role of this class is to figure
     * out what control to use to render a given socket.
     */
    public static <T> SocketPreviewView<T> createPreviewView(EventBus eventBus, Socket<T> socket) {
        // TODO: implement more types of previews
        return new TextAreaSocketPreviewView<>(eventBus, socket);
    }
}
