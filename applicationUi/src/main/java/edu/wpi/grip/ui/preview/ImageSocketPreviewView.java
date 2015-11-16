package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.ui.util.ImageConverter;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * A <code>SocketPreviewView</code> that previews sockets containing OpenCV Mats
 */
public class ImageSocketPreviewView extends SocketPreviewView<Mat> {

    private final ImageConverter imageConverter;
    private final ImageView imageView;

    /**
     * @param eventBus The EventBus used by the application
     * @param socket   An output socket to preview
     */
    public ImageSocketPreviewView(EventBus eventBus, OutputSocket<Mat> socket) {
        super(eventBus, socket);

        this.imageConverter = new ImageConverter();
        this.imageView = new ImageView();
        this.setContent(imageView);

        this.convertImage();
    }

    @Subscribe
    public void onSocketChanged(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            this.convertImage();
        }
    }

    private void convertImage() {
        synchronized (this) {
            final Mat mat = this.getSocket().getValue();

            final Image image = this.imageConverter.convert(mat);
            Platform.runLater(() -> {
                synchronized (this) {
                    this.imageView.setImage(image);
                }
            });
        }
    }
}
