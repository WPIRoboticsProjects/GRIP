
package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceHasPendingUpdateEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.http.ContextStore;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.ExceptionWitness;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Provides a way to generate a {@link Mat Mat} from an image that has been POSTed to the
 * internal HTTP server.
 * <p>
 * Note that multiple {@link HttpSource HttpSources} will all supply the same image
 * (or, more precisely, the same <i>reference</i> to a single image).
 * </p>
 */
@XStreamAlias("grip:HttpImage")
public class HttpSource extends Source {

  /**
   * Map of handlers to their paths to avoid having multiple handlers per path.
   */
  private static final Map<String, HttpImageHandler> handlers = new HashMap<>();

  private static final String PATH_PROPERTY = "image_upload_path";

  /**
   * HTTP handler. Fires callbacks when a new image has been POSTed to /GRIP/upload/image
   */
  private final HttpImageHandler imageHandler;

  private final OutputSocket<Mat> imageOutput;
  private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Image");
  private final Mat image = new Mat();
  private final Consumer<Mat> callback;
  private final EventBus eventBus;
  private String path;

  public interface Factory {
    HttpSource create(Properties properties);

    HttpSource create(String path);
  }

  @AssistedInject
  HttpSource(
      ExceptionWitness.Factory exceptionWitnessFactory,
      EventBus eventBus,
      OutputSocket.Factory osf,
      GripServer server,
      ContextStore store,
      @Assisted Properties properties) {
    this(exceptionWitnessFactory,
        eventBus,
        osf,
        server,
        store,
        properties.getProperty(PATH_PROPERTY));
  }

  @AssistedInject
  HttpSource(
      ExceptionWitness.Factory exceptionWitnessFactory,
      EventBus eventBus,
      OutputSocket.Factory osf,
      GripServer server,
      ContextStore store,
      @Assisted String path) {
    super(exceptionWitnessFactory);
    this.path = path;
    this.imageHandler = handlers.computeIfAbsent(path, p -> new HttpImageHandler(store, p));
    this.imageOutput = osf.create(outputHint);
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
    return path;
  }

  @Override
  protected List<OutputSocket> createOutputSockets() {
    return ImmutableList.of(
        imageOutput
    );
  }

  @Override
  protected boolean updateOutputSockets() {
    if (image.empty()) {
      // No data, don't bother converting
      return false;
    }
    imageOutput.setValue(opencv_imgcodecs.imdecode(image, opencv_imgcodecs.CV_LOAD_IMAGE_COLOR));
    return true;
  }

  @Override
  public Properties getProperties() {
    Properties properties = new Properties();
    properties.setProperty(PATH_PROPERTY, path);
    return properties;
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
