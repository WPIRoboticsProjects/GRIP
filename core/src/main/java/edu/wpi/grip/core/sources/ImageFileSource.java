package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.ImageLoadingUtility;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Provides a way to generate a {@link Mat} from an image on the filesystem.
 */
@XStreamAlias(value = "grip:ImageFile")
public final class ImageFileSource extends Source {

  private static final String PATH_PROPERTY = "path";

  private final String name;
  private final String path;
  private final SocketHint<Mat> imageOutputHint = SocketHints.Outputs.createMatSocketHint("Image");
  private final OutputSocket<Mat> outputSocket;

  /**
   * @param exceptionWitnessFactory Factory to create the exceptionWitness
   * @param file                    The location on the file system where the image exists.
   */
  @AssistedInject
  ImageFileSource(
      final OutputSocket.Factory outputSocketFactory,
      final ExceptionWitness.Factory exceptionWitnessFactory,
      @Assisted final File file) throws UnsupportedEncodingException {
    this(outputSocketFactory, exceptionWitnessFactory, file.getAbsolutePath());
  }

  @AssistedInject
  ImageFileSource(
      final OutputSocket.Factory outputSocketFactory,
      final ExceptionWitness.Factory exceptionWitnessFactory,
      @Assisted final Properties properties) {
    this(outputSocketFactory, exceptionWitnessFactory,
        properties.getProperty(PATH_PROPERTY));
  }


  private ImageFileSource(
      final OutputSocket.Factory outputSocketFactory,
      final ExceptionWitness.Factory exceptionWitnessFactory,
      final String path) {
    super(exceptionWitnessFactory);
    this.path = checkNotNull(path, "Path can not be null");
    this.name = Files.getNameWithoutExtension(this.path);
    this.outputSocket = outputSocketFactory.create(imageOutputHint);
  }

  /**
   * Performs the loading of the image from the file system.
   *
   * @throws IOException If the image fails to load from the filesystem
   */
  @Override
  public void initialize() throws IOException {
    this.loadImage(path);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public List<OutputSocket> createOutputSockets() {
    return ImmutableList.of(
        outputSocket
    );
  }

  @Override
  protected boolean updateOutputSockets() {
    // The image never changes so the socket will never need to be updated.
    return false;
  }

  @Override
  public Properties getProperties() {
    final Properties properties = new Properties();
    properties.setProperty(PATH_PROPERTY, this.path);
    return properties;
  }

  /**
   * Loads the image and posts an update to the {@link EventBus}
   *
   * @param path The location on the file system where the image exists.
   */
  private void loadImage(String path) throws IOException {
    this.loadImage(path, opencv_imgcodecs.IMREAD_COLOR);
  }

  private void loadImage(String path, final int flags) throws IOException {
    ImageLoadingUtility.loadImage(path, flags, this.outputSocket.getValue().get());
    this.outputSocket.setValue(this.outputSocket.getValue().get());
  }


  public interface Factory {
    ImageFileSource create(File file);

    ImageFileSource create(Properties properties);
  }
}
