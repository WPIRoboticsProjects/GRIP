package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceHasPendingUpdateEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.operations.network.MapNetworkReceiverFactory;
import edu.wpi.grip.core.operations.network.NetworkReceiver;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.ExceptionWitness;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;
import java.util.Properties;

/**
 * Provides a way to get a {@link Types Type} from a NetworkTable that GRIP is connected to.
 */
@XStreamAlias("grip:NetworkValue")
public class NetworkTableEntrySource extends Source {

  private static final String PATH_PROPERTY = "networktable_path";
  private static final String TYPE_PROPERTY = "BOOLEAN";

  private final OutputSocket output;
  private final String path;
  private final Types type;
  private NetworkReceiver networkReceiver;

  public interface Factory {
    NetworkTableEntrySource create(Properties properties);

    NetworkTableEntrySource create(String path, Types type);
  }

  public enum Types {
    BOOLEAN, NUMBER, STRING;

    @Override
    public String toString() {
      return super.toString().charAt(0) + super.toString().substring(1).toLowerCase();
    }

  }

  @AssistedInject
  NetworkTableEntrySource(
      EventBus eventBus,
      ExceptionWitness.Factory exceptionWitnessFactory,
      OutputSocket.Factory osf,
      @Named("ntManager") MapNetworkReceiverFactory networkReceiverFactory,
      @Assisted Properties properties) {
    this(eventBus,
        exceptionWitnessFactory,
        osf,
        networkReceiverFactory,
        properties.getProperty(PATH_PROPERTY),
        Types.valueOf(properties.getProperty(TYPE_PROPERTY)));
  }

  @AssistedInject
  NetworkTableEntrySource(
      EventBus eventBus,
      ExceptionWitness.Factory exceptionWitnessFactory,
      OutputSocket.Factory osf,
      @Named("ntManager") MapNetworkReceiverFactory networkReceiverFactory,
      @Assisted String path,
      @Assisted Types type) {
    super(exceptionWitnessFactory);
    this.path = path;
    this.type = type;
    networkReceiver = networkReceiverFactory.create(path);
    output = osf.create(createOutputSocket(type));

    networkReceiver.addListener(o -> eventBus.post(new SourceHasPendingUpdateEvent(this)));
  }

  @Override
  public String getName() {
    return path;
  }

  @Override
  protected List<OutputSocket> createOutputSockets() {
    return ImmutableList.of(
        output
    );
  }

  @Override
  protected boolean updateOutputSockets() {
    try {
      output.setValue(networkReceiver.getValue());
    } catch (ClassCastException ex) {
      getExceptionWitness().flagException(ex, getName() + " is not of type "
          + output.getSocketHint().getTypeLabel());
      return false;
    }
    return true;
  }

  @Override
  public Properties getProperties() {
    Properties properties = new Properties();
    properties.setProperty(PATH_PROPERTY, path);
    properties.setProperty(TYPE_PROPERTY, type.toString().toUpperCase());
    return properties;
  }

  @Override
  public void initialize() {
    updateOutputSockets();
  }

  @Subscribe
  public void onSourceRemovedEvent(SourceRemovedEvent event) {
    if (event.getSource() == this) {
      networkReceiver.close();
    }
  }

  /**
   * Create a SocketHint from the given type.
   *
   * @param type The type of SocketHint to create
   */
  private static SocketHint createOutputSocket(Types type) {
    switch (type) {
      case BOOLEAN:
        return SocketHints.Outputs.createBooleanSocketHint(Types.BOOLEAN.toString(), false);
      case NUMBER:
        return SocketHints.Outputs.createNumberSocketHint(Types.NUMBER.toString(), 0.0);
      case STRING:
        return SocketHints.Outputs.createStringSocketHint(Types.STRING.toString(), "");
      default:
        throw new IllegalArgumentException("Invalid NetworkTable source type");
    }
  }
}