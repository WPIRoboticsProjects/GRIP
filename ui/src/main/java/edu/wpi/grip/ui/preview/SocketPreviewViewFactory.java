package edu.wpi.grip.ui.preview;

import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.operations.composite.BlobsReport;
import edu.wpi.grip.core.operations.composite.ContoursReport;
import edu.wpi.grip.core.operations.composite.LinesReport;
import org.bytedeco.javacpp.IntPointer;

import static org.bytedeco.javacpp.opencv_core.*;


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
    public static <T> SocketPreviewView<T> createPreviewView(OutputSocket<T> socket) {
        if (socket.getSocketHint().getType() == Mat.class) {
            return (SocketPreviewView) new ImageSocketPreviewView((OutputSocket<Mat>) socket);
        } else if (socket.getSocketHint().getType() == Point.class || socket.getSocketHint().getType() == Size.class) {
            return (SocketPreviewView) new PointSizeSocketPreviewView((OutputSocket<IntPointer>) socket);
        } else if (socket.getSocketHint().getType() == ContoursReport.class) {
            return (SocketPreviewView) new ContoursSocketPreviewView((OutputSocket<ContoursReport>) socket);
        } else if (socket.getSocketHint().getType() == LinesReport.class) {
            return (SocketPreviewView) new LinesSocketPreviewView((OutputSocket<LinesReport>) socket);
        } else if (socket.getSocketHint().getType() == BlobsReport.class) {
            return (SocketPreviewView) new BlobsSocketPreviewView((OutputSocket<BlobsReport>) socket);
        } else {
            return new TextAreaSocketPreviewView<>(socket);
        }
    }
}
