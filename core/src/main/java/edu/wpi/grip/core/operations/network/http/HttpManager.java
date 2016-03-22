package edu.wpi.grip.core.operations.network.http;

import com.google.inject.Inject;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.operations.network.Manager;
import edu.wpi.grip.core.operations.network.MapNetworkPublisher;
import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 */
public class HttpManager implements Manager, MapNetworkPublisherFactory {

    private final GripServer server;

    @Inject
    public HttpManager(GripServer server) {
        this.server = server;
    }

    @Override
    public <T> MapNetworkPublisher<T> create(Set<String> keys) {
        return new HttpPublisher<>(server, keys);
    }

    private static final class HttpPublisher<P> extends MapNetworkPublisher<P> {

        private final GripServer server;
        private String name;

        HttpPublisher(final GripServer server, final Set<String> keys) {
            super(keys);
            this.server = server;
        }

        @Override
        protected void doPublish(Map<String, P> publishMap) {
            server.addDataSupplier(name, () -> publishMap);
        }

        @Override
        protected void doPublishSingle(P value) {
            server.removeDataSupplier(name);
            server.addDataSupplier(name, () -> value);
        }

        @Override
        protected void doPublish() {
            server.removeDataSupplier(name);
        }

        @Override
        protected void publishNameChanged(Optional<String> oldName, String newName) {
            oldName.ifPresent(server::removeDataSupplier);
            this.name = newName;
        }

        @Override
        public void close() {
            server.removeDataSupplier(name);
        }
    }
}
