package edu.wpi.grip.core;


import edu.wpi.grip.core.settings.SettingsProvider;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.InputSocketImpl;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.OutputSocketImpl;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.GripMode;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Installs modules that provides the basic construction and wiring of GRIP.
 */
public class GripBasicModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GripMode.class).toInstance(GripMode.HEADLESS);

    // Allow for just injecting the settings provider, instead of the whole pipeline
    bind(SettingsProvider.class).to(Pipeline.class);

    install(new FactoryModuleBuilder().build(new TypeLiteral<Connection.Factory<Object>>() {
    }));

    bind(ConnectionValidator.class).to(Pipeline.class);

    bind(InputSocket.Factory.class).to(InputSocketImpl.FactoryImpl.class);
    bind(OutputSocket.Factory.class).to(OutputSocketImpl.FactoryImpl.class);
    install(new FactoryModuleBuilder().build(ExceptionWitness.Factory.class));
  }
}
