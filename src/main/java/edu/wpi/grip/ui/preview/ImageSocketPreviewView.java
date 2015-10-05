package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.primitives.UnsignedBytes;
import edu.wpi.grip.core.Socket;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * A <code>SocketPreviewView</code> that previews sockets containing OpenCV Mats
 */
public class ImageSocketPreviewView extends SocketPreviewView<Mat> {

    private final ImageView imageView;
    private WritableImage image;
    private IntBuffer pixels;

    /**
     * @param eventBus The EventBus used by the application
     * @param socket   An output socket to preview
     */
    public ImageSocketPreviewView(EventBus eventBus, Socket<Mat> socket) {
        super(eventBus, socket);

        this.convertImage();


        this.imageView = new ImageView(this.image);
        this.setContent(imageView);

        // Every time the Mat changes, convert it into a JavaFX image and show it.
        this.valueProperty().addListener(observable -> Platform.runLater(() -> {
            this.convertImage();
            this.imageView.setImage(this.image);
        }));
    }

    /**
     * Convert a BGR-formatted OpenCV {@link Mat} into a JavaFX {@link Image}.
     * <p>
     * JavaFX understands ARGB pixel data, so one way to turn a Mat into a JavaFX image is to shift around the bytes
     * from the Mat into an int array of pixels.
     * <p>
     * This is also possible to do by using JavaCV, but the JavaCV method involves several intermediate conversions
     * (Mat -> Frame -> BufferedImage -> JavaFX Image) and is way too slow to use for a real-time video.
     */
    private void convertImage() {
        final Mat mat = this.valueProperty().get();
        final int width = mat.cols();
        final int height = mat.rows();
        final int channels = mat.channels();

        assert channels == 3 : "Only 3-channel/BGR images can be previewed";

        // If the size of the Mat changed for whatever reason, allocate a new image with the proper dimensions and a buffer
        // big enough to hold all of the pixels in the image.
        if (this.image == null || this.image.getWidth() != width || this.image.getHeight() != height) {
            this.image = new WritableImage(width, height);
            this.pixels = IntBuffer.allocate(width * height);
        }

        // Convert the BGR data from the Mat into ARGB data that we can put into a JavaFX WritableImage
        // TODO: Also add functions for converting binary and grayscale data into ARGB
        ByteBuffer buffer = mat.<ByteBuffer>createBuffer();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int b = UnsignedBytes.toInt(buffer.get((width * y + x) * channels));
                final int g = UnsignedBytes.toInt(buffer.get((width * y + x) * channels + 1));
                final int r = UnsignedBytes.toInt(buffer.get((width * y + x) * channels + 2));
                this.pixels.put(width * y + x, (0xff << 24) | (r << 16) | (g << 8) | b);
            }
        }

        final PixelFormat<IntBuffer> argb = PixelFormat.getIntArgbInstance();
        this.image.getPixelWriter().setPixels(0, 0, width, height, argb, this.pixels, width);
    }
}
