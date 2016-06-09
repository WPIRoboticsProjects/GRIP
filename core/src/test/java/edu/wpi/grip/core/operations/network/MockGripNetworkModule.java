package edu.wpi.grip.core.operations.network;

import edu.wpi.grip.core.operations.network.ros.MockROSManager;
import edu.wpi.grip.core.operations.network.ros.ROSNetworkPublisherFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * A mock of {@Link GRIPNetworkModule} for testing.
 */
public final class MockGripNetworkModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MapNetworkPublisherFactory.class)
            .annotatedWith(Names.named("ntManager"))
            .to(MockMapNetworkPublisher.class);
        bind(ROSNetworkPublisherFactory.class)
            .annotatedWith(Names.named("rosManager"))
            .to(MockROSManager.class);
    }
}
