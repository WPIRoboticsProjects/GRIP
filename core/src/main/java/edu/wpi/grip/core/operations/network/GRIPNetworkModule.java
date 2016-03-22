package edu.wpi.grip.core.operations.network;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import edu.wpi.grip.core.operations.network.networktables.NTManager;
import edu.wpi.grip.core.operations.network.ros.ROSManager;
import edu.wpi.grip.core.operations.network.ros.ROSNetworkPublisherFactory;

/**
 * Defines any concrete implementation mappings between
 * network managers and their real counterparts.
 * This module should not be used in tests as networking protocols
 * can define "Static State" that can cause other tests to fail in
 * unexpected ways if not properly cleaned up.
 */
public final class GRIPNetworkModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MapNetworkPublisherFactory.class)
                .annotatedWith(Names.named("ntManager"))
                .to(NTManager.class);
        bind(ROSNetworkPublisherFactory.class)
                .annotatedWith(Names.named("rosManager"))
                .to(ROSManager.class);
    }
}
