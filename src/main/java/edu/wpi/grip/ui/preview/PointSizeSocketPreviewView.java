package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.bytedeco.javacpp.IntPointer;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * A {@link SocketPreviewView} for OpenCV points and sizes
 */
public class PointSizeSocketPreviewView extends SocketPreviewView<IntPointer> {

    private final TextField x, y;

    /**
     * @param eventBus The EventBus used by the application
     * @param socket   An output socket to preview
     */
    public PointSizeSocketPreviewView(EventBus eventBus, OutputSocket<IntPointer> socket) {
        super(eventBus, socket);

        x = new TextField();
        x.setEditable(false);
        y = new TextField();
        y.setEditable(false);

        final GridPane gridPane = new GridPane();
        gridPane.add(x, 1, 0);
        gridPane.add(y, 1, 1);

        // The only difference between point and size previews is the labels
        if (socket.getSocketHint().getType().equals(Point.class)) {
            gridPane.add(new Label("x: "), 0, 0);
            gridPane.add(new Label("y: "), 0, 1);
        } else if (socket.getSocketHint().getType().equals(Size.class)) {
            gridPane.add(new Label("width: "), 0, 0);
            gridPane.add(new Label("height: "), 0, 1);
        }

        this.updateTextFields();
        this.setContent(gridPane);
    }

    @Subscribe
    public void onSocketChanged(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            this.updateTextFields();
        }
    }

    private void updateTextFields() {
        this.x.setText("" + this.getSocket().getValue().get().get(0));
        this.y.setText("" + this.getSocket().getValue().get().get(1));
    }
}
