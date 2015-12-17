package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.ui.util.GRIPPlatform;
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
     * @param socket   An output socket to preview
     */
    ImageSocketPreviewView(OutputSocket<Mat> socket) {
        super(socket);

        this.imageConverter = new ImageConverter();
        this.imageView = new ImageView();
        this.setContent(imageView);

        assert Platform.isFxApplicationThread() : "Must be in FX Thread to create this or you will be exposing constructor to another thread!";
        convertImage();
    }

    @Subscribe
    public void onSocketChanged(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            this.convertImage();
        }
    }

    private void convertImage() {
        synchronized (this) {
            this.getSocket().getValue().ifPresent(mat -> {
                final Image image = this.imageConverter.convert(mat);
                GRIPPlatform.runAndWait(() -> {
                    this.imageView.setImage(image);
                });

            });
        }
    }
}
