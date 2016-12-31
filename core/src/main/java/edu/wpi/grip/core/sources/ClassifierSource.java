package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.util.ExceptionWitness;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * A source for the path to a XML classifier file (e.g. haarcascade_face_default.xml).
 */
@XStreamAlias("grip:Classifier")
public class ClassifierSource extends Source {

  private final String filePath;
  private static final String FILE_PATH_PROPERTY = "file_path";

  private final SocketHint<CascadeClassifier> classifierHint =
      new SocketHint.Builder<>(CascadeClassifier.class)
          .identifier("Classifier")
          .build();
  private final OutputSocket<CascadeClassifier> classifierSocket;

  @AssistedInject
  protected ClassifierSource(EventBus eventBus,
                             OutputSocket.Factory osf,
                             ExceptionWitness.Factory exceptionWitnessFactory,
                             @Assisted String filePath) {
    super(exceptionWitnessFactory);
    this.classifierSocket = osf.create(classifierHint);
    this.filePath = filePath;
  }

  @AssistedInject
  protected ClassifierSource(EventBus eventBus,
                             OutputSocket.Factory osf,
                             ExceptionWitness.Factory exceptionWitnessFactory,
                             @Assisted Properties properties) {
    this(eventBus, osf, exceptionWitnessFactory, properties.getProperty(FILE_PATH_PROPERTY));
  }

  @Override
  public String getName() {
    return "Classifier source";
  }

  @Override
  protected List<OutputSocket> createOutputSockets() {
    return ImmutableList.of(
        classifierSocket
    );
  }

  @Override
  protected boolean updateOutputSockets() {
    // The path is constant, so always return false
    return false;
  }

  @Override
  public Properties getProperties() {
    Properties properties = new Properties();
    properties.put(FILE_PATH_PROPERTY, filePath);
    return properties;
  }

  @Override
  public void initialize() throws IOException {
    if (!Files.exists(Paths.get(filePath))) {
      throw new IOException("File does not exist: " + filePath);
    }
    try {
      classifierSocket.setValue(new CascadeClassifier(filePath));
    } catch (Exception e) {
      // OpenCV will throw an exception if given malformed XML
      throw new IOException("Could not load cascade classifier XML", e);
    }
  }

  public interface Factory {

    ClassifierSource create(String path);

    ClassifierSource create(Properties properties);
  }

}
