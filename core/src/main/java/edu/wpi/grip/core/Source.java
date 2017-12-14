package edu.wpi.grip.core;

import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ClassifierSource;
import edu.wpi.grip.core.sources.HttpSource;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;
import edu.wpi.grip.core.sources.NetworkTableEntrySource;
import edu.wpi.grip.core.sources.VideoFileSource;
import edu.wpi.grip.core.util.ExceptionWitness;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for an input into the pipeline.
 */
public abstract class Source {
  private static final Logger logger = Logger.getLogger(Source.class.getName());
  private final ExceptionWitness exceptionWitness;

  /**
   * @param exceptionWitnessFactory Factory to create the exceptionWitness.
   */
  protected Source(ExceptionWitness.Factory exceptionWitnessFactory) {
    this.exceptionWitness = exceptionWitnessFactory.create(this);
  }

  /**
   * This is used by the GUI to distinguish different sources. For example, {@link
   * edu.wpi.grip.core.sources.ImageFileSource} returns the filename of the image.
   *
   * @return The name of this source.
   */
  public abstract String getName();

  /**
   * Get the sockets for this source.
   *
   * @return @return An array of {@link OutputSocket}s for the outputs that the source produces.
   */
  public final ImmutableList<OutputSocket> getOutputSockets() {
    final List<OutputSocket> outputSockets = createOutputSockets();
    for (OutputSocket socket : outputSockets) {
      socket.setSource(Optional.of(this));
    }

    return ImmutableList.copyOf(outputSockets);
  }

  protected abstract List<OutputSocket> createOutputSockets();

  /**
   * This method will check if there are any pending updates to output sockets. If there are any,
   * update the sockets and then return true. If there are no updates this function should return
   * false.
   *
   * @return true if there are updates ready to be moved into the socket.
   */
  protected abstract boolean updateOutputSockets();

  /**
   * This is used for serialization/deserialization.
   *
   * @return A {@link Properties} containing data that can be used to re-create this source.
   */
  public abstract Properties getProperties();

  protected ExceptionWitness getExceptionWitness() {
    return this.exceptionWitness;
  }

  /**
   * Initializes the source. This should not try to handle initialization exceptions. Instead, the
   * {@link #initializeSafely()} should report the problem with initializing to the exception
   * witness.
   */
  public abstract void initialize() throws IOException;

  /**
   * Initializes the source in a safe way such that the exception caused by initializing will be
   * reported to the {@link ExceptionWitness}. This method should be used by the deserializer to
   * ensure that a source that is invalid can display this info to the UI and allow the user to
   * modify the save file.
   */
  public final void initializeSafely() {
    try {
      initialize();
    } catch (IOException e) {
      final String message = "Failed to initialize " + getClass().getSimpleName();
      logger.log(Level.WARNING, message, e);
      getExceptionWitness().flagException(e, message);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", getName())
        .toString();
  }

  public interface SourceFactory {
    Source create(Class<?> type, Properties properties) throws IOException;
  }

  public static class SourceFactoryImpl implements SourceFactory {
    @Inject
    CameraSource.Factory cameraFactory;
    @Inject
    ImageFileSource.Factory imageFactory;
    @Inject
    MultiImageFileSource.Factory multiImageFactory;
    @Inject
    HttpSource.Factory httpFactory;
    @Inject
    NetworkTableEntrySource.Factory networkTableEntryFactory;
    @Inject
    ClassifierSource.Factory fileSourceFactory;
    @Inject
    VideoFileSource.Factory videoFileSourceFactory;

    @Override
    public Source create(Class<?> type, Properties properties) throws IOException {
      if (type.isAssignableFrom(CameraSource.class)) {
        return cameraFactory.create(properties);
      } else if (type.isAssignableFrom(ImageFileSource.class)) {
        return imageFactory.create(properties);
      } else if (type.isAssignableFrom(MultiImageFileSource.class)) {
        return multiImageFactory.create(properties);
      } else if (type.isAssignableFrom(HttpSource.class)) {
        return httpFactory.create(properties);
      } else if (type.isAssignableFrom(NetworkTableEntrySource.class)) {
        return networkTableEntryFactory.create(properties);
      } else if (type.isAssignableFrom(ClassifierSource.class)) {
        return fileSourceFactory.create(properties);
      } else if (type.isAssignableFrom(VideoFileSource.class)) {
        return videoFileSourceFactory.create(properties);
      } else {
        throw new IllegalArgumentException(type + " was not a valid type");
      }
    }
  }

}
