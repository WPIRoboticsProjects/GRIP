package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.UnsignedBytes;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_core.CV_8S;
import static org.bytedeco.javacpp.opencv_core.CV_8U;
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
    public ImageSocketPreviewView(EventBus eventBus, OutputSocket<Mat> socket) {
        super(eventBus, socket);

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
        final Mat mat = this.getSocket().getValue();
        final int width = mat.cols();
        final int height = mat.rows();
        final int channels = mat.channels();

        assert channels == 3 || channels == 1 :
                "Only 3-channel BGR images or single-channel grayscale images can be previewed";

        assert mat.type() == CV_8U || mat.type() == CV_8S :
                "Only images with 8 bits per channel can be previewed";

        // Don't try to render empty images.
        if (mat.empty()) {
            this.imageView.setImage(null);
            return;
        }

        // If the size of the Mat changed for whatever reason, allocate a new image with the proper dimensions and a buffer
        // big enough to hold all of the pixels in the image.
        if (this.image == null || this.image.getWidth() != width || this.image.getHeight() != height) {
            this.image = new WritableImage(width, height);
            this.pixels = IntBuffer.allocate(width * height);
        }

        final ByteBuffer buffer = mat.<ByteBuffer>createBuffer();
        final int stride = buffer.capacity() / height;

        // Convert the data from the Mat into ARGB data that we can put into a JavaFX WritableImage
        switch (channels) {
            case 1:
                // 1 channel - convert grayscale to ARGB
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        final int value = UnsignedBytes.toInt(buffer.get(stride * y + channels * x));
                        this.pixels.put(width * y + x, (0xff << 24) | (value << 16) | (value << 8) | value);
                    }
                }

                break;

            case 3:
                // 3 channels - convert BGR to RGBA
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        final int b = UnsignedBytes.toInt(buffer.get(stride * y + channels * x));
                        final int g = UnsignedBytes.toInt(buffer.get(stride * y + channels * x + 1));
                        final int r = UnsignedBytes.toInt(buffer.get(stride * y + channels * x + 2));
                        this.pixels.put(width * y + x, (0xff << 24) | (r << 16) | (g << 8) | b);
                    }
                }

                break;
        }

        final PixelFormat<IntBuffer> argb = PixelFormat.getIntArgbInstance();
        this.image.getPixelWriter().setPixels(0, 0, width, height, argb, this.pixels, width);
        this.imageView.setImage(this.image);
    }
}
