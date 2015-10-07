package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;

import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * Factory for constructing {@link SocketPreviewView}s
 */
public class SocketPreviewViewFactory {

    /**
     * Create an instance of {@link SocketPreviewView} appropriate for the given socket.  Sockets of different types
     * (like numbers, arrays, images, etc...) are rendered in different ways, and the role of this class is to figure
     * out what control to use to render a given socket.
     */
    @SuppressWarnings("unchecked")
    public static <T> SocketPreviewView<T> createPreviewView(EventBus eventBus, OutputSocket<T> socket) {
        if (socket.getSocketHint().getType() == Mat.class) {
            return (SocketPreviewView) new ImageSocketPreviewView(eventBus, (OutputSocket<Mat>) socket);
        } else {
            return new TextAreaSocketPreviewView<>(eventBus, socket);
        }
    }
}
