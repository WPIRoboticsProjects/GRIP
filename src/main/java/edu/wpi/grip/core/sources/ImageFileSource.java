package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.Source;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Provides a way to generate a {@link Mat} from an image on the filesystem.
 */
@XStreamAlias(value = "grip:ImageFile")
public class ImageFileSource extends Source {

    private final String PATH_PROPERTY = "path";

    private String name;
    private String path;
    private final SocketHint<Mat> imageOutputHint = new SocketHint<Mat>("Image", Mat.class, Mat::new);
    private OutputSocket<Mat> outputSocket;

    /**
     * @param eventBus The event bus for the pipeline.
     * @param file     The location on the file system where the image exists.
     */
    public ImageFileSource(EventBus eventBus, File file) throws IOException {
        this.initialize(eventBus, URLDecoder.decode(Paths.get(file.toURI()).toString()));
    }

    /**
     * Used for serialization
     */
    public ImageFileSource() {
    }

    private void initialize(EventBus eventBus, String path) throws IOException {
        checkNotNull(eventBus, "Event Bus was null.");
        checkNotNull(path, "Path was null");

        this.path = path;
        this.name = Files.getNameWithoutExtension(this.path);
        this.outputSocket = new OutputSocket<>(eventBus, imageOutputHint);

        this.loadImage(path);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public OutputSocket[] createOutputSockets() {
        return new OutputSocket[]{this.outputSocket};
    }

    @Override
    public Properties getProperties() {
        final Properties properties = new Properties();
        properties.setProperty(PATH_PROPERTY, this.path);
        return properties;
    }

    @Override
    public void createFromProperties(EventBus eventBus, Properties properties) throws IOException {
        final String path = properties.getProperty(PATH_PROPERTY);
        if (path == null) {
            throw new IllegalArgumentException("Cannot create ImageFileSource without a path.");
        }

        this.initialize(eventBus, path);
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
