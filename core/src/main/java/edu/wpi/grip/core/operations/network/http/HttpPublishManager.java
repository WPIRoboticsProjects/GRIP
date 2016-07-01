package edu.wpi.grip.core.operations.network.http;

import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.operations.network.Manager;
import edu.wpi.grip.core.operations.network.MapNetworkPublisher;
import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manager for publishing data to the internal HTTP server.
 */
@Singleton
public class HttpPublishManager implements Manager, MapNetworkPublisherFactory {

  private final DataHandler dataHandler;

  @Inject
  public HttpPublishManager(GripServer server, DataHandler dataHandler) {
    this.dataHandler = dataHandler;
    server.addHandler(dataHandler);
  }

  @Override
  public <T> MapNetworkPublisher<T> create(Set<String> keys) {
    return new HttpPublisher<>(dataHandler, keys);
  }

  private static final class HttpPublisher<P> extends MapNetworkPublisher<P> {

    private final DataHandler dataHandler;
    private String name;

    HttpPublisher(final DataHandler dataHandler, final Set<String> keys) {
      super(keys);
      this.dataHandler = dataHandler;
    }

    @Override
    protected void doPublish() {
      close();
    }

    @Override
    protected void doPublish(Map<String, P> publishMap) {
      dataHandler.addDataSupplier(name, () -> publishMap);
    }

    @Override
    protected void doPublishSingle(P value) {
      dataHandler.removeDataSupplier(name);
      dataHandler.addDataSupplier(name, () -> value);
    }

    @Override
    protected void publishNameChanged(Optional<String> oldName, String newName) {
      oldName.ifPresent(dataHandler::removeDataSupplier);
      this.name = newName;
    }

    @Override
    public void close() {
      dataHandler.removeDataSupplier(name);
    }
  }
}
