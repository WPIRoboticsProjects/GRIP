
package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.SocketHints;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceHasPendingUpdateEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.PostHandler;
import edu.wpi.grip.core.util.ExceptionWitness;

import java.util.Properties;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;

/**
 * Provides a way to generate a constantly updated {@link Mat} from the internal HTTP server.
 */
public class HttpSource extends Source implements PostHandler {

    private final OutputSocket<Mat> imageOutput;
    private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Image");
    private byte[] data = new byte[0];
    private boolean gotImage = false;

    private final EventBus eventBus;
    private final GripServer server;

    public interface Factory {
        HttpSource create();
    }

    @Inject
    HttpSource(
            ExceptionWitness.Factory exceptionWitnessFactory,
            EventBus eventBus,
            GripServer server) {
        super(exceptionWitnessFactory);
        this.eventBus = eventBus;
        this.server = server;
        this.imageOutput = new OutputSocket<>(eventBus, outputHint);
        System.out.println("Creating new HttpSource");
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
        if (!gotImage) {
            return false;
        }
        if (data.length == 0) {
            // Got data, but it's empty
            return false;
        }
        gotImage = false;
        imageOutput.setValue(opencv_imgcodecs.imdecode(new Mat(data), opencv_imgcodecs.CV_LOAD_IMAGE_COLOR));
        return true;
    }

    @Override
    public Properties getProperties() {
        // Don't need any special properties; everything is handled by the server
        // (Change this if the image upload path is user-configurable)
        return new Properties();
    }

    @Override
    public void initialize() {
        server.addPostHandler(GripServer.IMAGE_UPLOAD_PATH, this);
    }

    @Subscribe
    public void onSourceRemovedEvent(SourceRemovedEvent event) {
        if (event.getSource() == this) {
            server.removePostHandler(this);
        }
    }

    @Override
    public boolean convert(byte[] bytes) {
        data = bytes;
        gotImage = true;
        eventBus.post(new SourceHasPendingUpdateEvent(this));
        return true;
    }

}
