package edu.wpi.grip.core.operations.network;

import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.HttpPipelineSwitcher;
import edu.wpi.grip.core.operations.network.http.DataHandler;
import edu.wpi.grip.core.operations.network.http.HttpPublishManager;
import edu.wpi.grip.core.operations.network.networktables.NTManager;
import edu.wpi.grip.core.operations.network.ros.ROSManager;
import edu.wpi.grip.core.operations.network.ros.ROSNetworkPublisherFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Defines any concrete implementation mappings between network managers and their real
 * counterparts. This module should not be used in tests as networking protocols can define "Static
 * State" that can cause other tests to fail in unexpected ways if not properly cleaned up.
 */
public final class GripNetworkModule extends AbstractModule {
  @Override
  protected void configure() {
    // HTTP server injection bindings
    bind(GripServer.JettyServerFactory.class).to(GripServer.JettyServerFactoryImpl.class);
    bind(GripServer.class).asEagerSingleton();
    bind(HttpPipelineSwitcher.class).asEagerSingleton();
    bind(DataHandler.class).asEagerSingleton();
    // Network publishing bindings
    bind(MapNetworkPublisherFactory.class)
        .annotatedWith(Names.named("ntManager"))
        .to(NTManager.class);
    bind(MapNetworkPublisherFactory.class)
        .annotatedWith(Names.named("httpManager"))
        .to(HttpPublishManager.class);
    bind(ROSNetworkPublisherFactory.class)
        .annotatedWith(Names.named("rosManager"))
        .to(ROSManager.class);

    // Network receiver bindings
    bind(MapNetworkReceiverFactory.class)
        .annotatedWith(Names.named("ntManager"))
        .to(NTManager.class);
  }
}
