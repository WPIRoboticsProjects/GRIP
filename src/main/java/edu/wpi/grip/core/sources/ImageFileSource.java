package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.Source;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.net.URL;
import java.net.URLDecoder;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Provides a way to generate a {@link Mat} from an image on the filesystem.
 */
public class ImageFileSource implements Source {

    private final String name;
    private final SocketHint<Mat> imageOutputHint = new SocketHint<Mat>("Image", Mat.class, Mat::new);
    private final OutputSocket<Mat> outputSocket;

    /**
     * @param eventBus The event bus for the pipeline.
     * @param imageURL The location on the file system where the image exists.
     */
    public ImageFileSource(EventBus eventBus, URL imageURL) {
        checkNotNull(eventBus, "Event Bus was null.");

        this.name = imageURL.toString().substring(imageURL.toString().lastIndexOf('/') + 1);
        this.outputSocket = new OutputSocket<Mat>(eventBus, imageOutputHint);

        this.loadImage(imageURL);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public OutputSocket[] getOutputSockets() {
        return new OutputSocket[]{this.outputSocket};
    }

    /**
     * Loads the image and posts an update to the {@link EventBus}
     *
     * @param imageURL The location on the file system where the image exists.
     */
    private void loadImage(URL imageURL) {
        this.loadImage(imageURL, opencv_imgcodecs.IMREAD_COLOR);
    }

    /**
     * Loads the image and posts an update to the {@link EventBus}
     *
     * @param imageURL The location on the file system where the image exists.
     * @param flags    Flags to pass to imread {@link opencv_imgcodecs#imread(String, int)}
     */
    private void loadImage(URL imageURL, final int flags) {
        checkNotNull(imageURL, "Image URL was null.");

        final String path = URLDecoder.decode(imageURL.getPath());

        Mat image = opencv_imgcodecs.imread(path, flags);
        if (!image.empty()) {
            this.outputSocket.setValue(image);
        } else {
            // TODO Output Error to GUI about invalid url
            new Exception("Error loading image " + path).printStackTrace(System.err);
        }
    }
}
