package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.ExceptionWitness;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;
import java.util.Properties;

/**
 * A source for a single file's path. This will only supply the <i>path</i> to the file; it is up
 * to the operations to resolve the file associated with the path.
 */
@XStreamAlias("grip:File")
public class FileSource extends Source {

  private final String filePath;
  private static final String FILE_PATH_PROPERTY = "file_path";

  private final SocketHint<String> pathHint
      = SocketHints.Outputs.createStringSocketHint("Path", "");
  private final OutputSocket<String> pathSocket;

  @AssistedInject
  protected FileSource(EventBus eventBus,
                       OutputSocket.Factory osf,
                       ExceptionWitness.Factory exceptionWitnessFactory,
                       @Assisted String filePath) {
    super(exceptionWitnessFactory);
    this.pathSocket = osf.create(pathHint);
    this.filePath = filePath;
  }

  @AssistedInject
  protected FileSource(EventBus eventBus,
                       OutputSocket.Factory osf,
                       ExceptionWitness.Factory exceptionWitnessFactory,
                       @Assisted Properties properties) {
    this(eventBus, osf, exceptionWitnessFactory, properties.getProperty(FILE_PATH_PROPERTY));
  }

  @Override
  public String getName() {
    return "File source";
  }

  @Override
  protected List<OutputSocket> createOutputSockets() {
    return ImmutableList.of(
        pathSocket
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
  public void initialize() {
    pathSocket.setValue(filePath);
  }

  public interface Factory {

    FileSource create(String path);

    FileSource create(Properties properties);
  }

}
