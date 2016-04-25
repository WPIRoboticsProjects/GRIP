
package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceHasPendingUpdateEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.ExceptionWitness;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;

/**
 * Provides a way to generate a {@link Mat Mat} from an image that has been POSTed to the internal HTTP server.
 * <p>
 * Note that multiple {@link HttpSource HttpSources} will all supply the same image (or, more precisely, the same
 * <i>reference</i> to a single image).
 * </p>
 */
@XStreamAlias("grip:HttpImage")
public class HttpSource extends Source {

    /**
     * Map of handlers to their paths to avoid having multiple handlers per path.
     */
    private static final Map<String, HttpImageHandler> handlers = new HashMap<>();

    /**
     * HTTP handler. Fires callbacks when a new image has been POSTed to /GRIP/upload/image
     */
    private final HttpImageHandler imageHandler;

    private final OutputSocket<Mat> imageOutput;
    private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Image");
    private final Mat image = new Mat();
    private final Consumer<Mat> callback;
    private final EventBus eventBus;

    public interface Factory {
        HttpSource create();
    }

    @Inject
    HttpSource(
            ExceptionWitness.Factory exceptionWitnessFactory,
            EventBus eventBus,
            GripServer server) {
        super(exceptionWitnessFactory);
        final String path = GripServer.IMAGE_UPLOAD_PATH; //TODO make this user-configurable
        this.imageHandler = handlers.computeIfAbsent(path, HttpImageHandler::new);
        this.imageOutput = new OutputSocket<>(eventBus, outputHint);
        this.eventBus = eventBus;
        // Will add the handler only when the first HttpSource is created -- no-op every subsequent time
        // (Otherwise, multiple handlers would be getting called and it'd be a mess)
        server.addHandler(imageHandler);
        this.callback = this::setImage;
    }

    private void setImage(Mat image) {
        image.copyTo(this.image);
        eventBus.post(new SourceHasPendingUpdateEvent(this));
    }

    @Override
    public String getName() {
        return "HTTP source";
    }

    @Override
    protected OutputSocket[] createOutputSockets() {
        return new OutputSocket[]{imageOutput};
    }

    @Override
    protected boolean updateOutputSockets() {
        imageOutput.setValue(opencv_imgcodecs.imdecode(image, opencv_imgcodecs.CV_LOAD_IMAGE_COLOR));
        return true;
    }

    @Override
    public Properties getProperties() {
        // Don't need any special properties; everything is handled by the server
        // TODO make the image upload path user-configurable
        return new Properties();
    }

    @Override
    public void initialize() {
        imageHandler.addCallback(callback);
        imageHandler.getImage().ifPresent(this::setImage);
    }

    @Subscribe
    public void onSourceRemovedEvent(SourceRemovedEvent event) {
        if (event.getSource() == this) {
            imageHandler.removeCallback(callback);
        }
    }

}
