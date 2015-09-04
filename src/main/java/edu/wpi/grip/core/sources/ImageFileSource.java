package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.Source;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Provides a way to generate a {@link Mat} from an image on the filesystem.
 */
public class ImageFileSource implements Source {
    private final SocketHint imageOutputHint = new SocketHint("Image", Mat.class);
    private final Socket<Mat> outputSocket;

    /**
     *
     * @param eventBus The event bus for the pipeline.
     */
    public ImageFileSource(EventBus eventBus){
        checkNotNull(eventBus, "Event Bus was null.");
        this.outputSocket = new Socket(eventBus, imageOutputHint);
    }

    @Override
    public Socket<Mat>[] getOutputSockets() {
        return new Socket[]{this.outputSocket};
    }

    /**
     * Loads the image and posts an update to the {@link EventBus}
     * @param imageURL The location on the file system where the image exists.
     */
    public void loadImage(URL imageURL){
        this.loadImage(imageURL, opencv_imgcodecs.IMREAD_COLOR);
    }

    /**
     * Loads the image and posts an update to the {@link EventBus}
     * @param imageURL The location on the file system where the image exists.
     * @param flags Flags to pass to imread {@link opencv_imgcodecs#imread(String, int)}
     */
    public void loadImage(URL imageURL, final int flags /*=cv::IMREAD_COLOR*/){
        checkNotNull(imageURL, "Image URL was null.");

        Mat image = opencv_imgcodecs.imread(imageURL.getPath(), flags);
        if(!image.empty()) {
            this.outputSocket.setValue(image);
        } else {
            //TODO Output Error to GUI about invalid url
            new Exception("The Mat returned by opencv was empty.").printStackTrace(System.err);
        }
    }

}
