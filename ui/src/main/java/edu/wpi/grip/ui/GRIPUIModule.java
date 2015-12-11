package edu.wpi.grip.ui;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import edu.wpi.grip.ui.pipeline.StepController;

/**
 * A Guice {@link com.google.inject.Module} for GRIP's UI package.
 */
public class GRIPUIModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(StepController.class, StepController.class)
                .build(StepController.Factory.class));
    }
}
