package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.Source;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Paths;

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
     * @param file     The location on the file system where the image exists.
     */
    public ImageFileSource(EventBus eventBus, File file) throws IOException {
        checkNotNull(eventBus, "Event Bus was null.");
        checkNotNull(file, "Image was null");

        final String path = URLDecoder.decode(Paths.get(file.toURI()).toString());

        this.name = Files.getNameWithoutExtension(path);
        this.outputSocket = new OutputSocket<Mat>(eventBus, imageOutputHint);


        this.loadImage(path);
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
     * @param path The location on the file system where the image exists.
     */
    private void loadImage(String path) throws IOException {
        this.loadImage(path, opencv_imgcodecs.IMREAD_COLOR);
    }

    /**
     * Loads the image and posts an update to the {@link EventBus}
     *
     * @param path  The location on the file system where the image exists.
     * @param flags Flags to pass to imread {@link opencv_imgcodecs#imread(String, int)}
     */
    private void loadImage(String path, final int flags) throws IOException {
        Mat mat = opencv_imgcodecs.imread(path, flags);
        if (!mat.empty()) {
            mat.copyTo(this.outputSocket.getValue());
            this.outputSocket.setValue(this.outputSocket.getValue());
        } else {
            // TODO Output Error to GUI about invalid url
            throw new IOException("Error loading image " + path);
        }
    }
}
