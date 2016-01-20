package edu.wpi.grip.ui;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.components.ExceptionWitnessResponderButton;
import edu.wpi.grip.ui.components.StartStoppableButton;
import edu.wpi.grip.ui.pipeline.OutputSocketController;
import edu.wpi.grip.ui.pipeline.SocketHandleView;
import edu.wpi.grip.ui.pipeline.StepController;
import edu.wpi.grip.ui.pipeline.input.*;
import edu.wpi.grip.ui.pipeline.source.CameraSourceController;
import edu.wpi.grip.ui.pipeline.source.MultiImageFileSourceController;
import edu.wpi.grip.ui.pipeline.source.SourceController;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

/**
 * A Guice {@link com.google.inject.Module} for GRIP's UI package.
 */
public class GRIPUIModule extends AbstractModule {
    @Override
    protected void configure() {

        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(final TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
                typeEncounter.register((InjectionListener<I>) i -> {
                    if (!i.getClass().isAnnotationPresent(ParametrizedController.class)) {
                        return;
                    }
                    try {
                        FXMLLoader.load(i.getClass().getResource(
                                i.getClass().getAnnotation(ParametrizedController.class).url()),
                                null, null,
                                c -> i
                        );
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to load FXML", e);
                    }
                });
            }
        });


        install(new FactoryModuleBuilder()
                .implement(StepController.class, StepController.class)
                .build(StepController.Factory.class));

        // Source Factories
        install(new FactoryModuleBuilder().build(new TypeLiteral<SourceController.BaseSourceControllerFactory<Source>>() {
        }));
        install(new FactoryModuleBuilder().build(MultiImageFileSourceController.Factory.class));
        install(new FactoryModuleBuilder().build(CameraSourceController.Factory.class));
        // END Source Factories

        // Components
        install(new FactoryModuleBuilder().build(StartStoppableButton.Factory.class));
        install(new FactoryModuleBuilder().build(ExceptionWitnessResponderButton.Factory.class));
        // End Components

        // Controllers
        install(new FactoryModuleBuilder().build(OperationController.Factory.class));
        install(new FactoryModuleBuilder().build(SocketHandleView.Factory.class));
        install(new FactoryModuleBuilder().build(OutputSocketController.Factory.class));
        // End arbitrary controllers

        // InputSocketController Factories
        install(new FactoryModuleBuilder().build(new TypeLiteral<InputSocketController.BaseInputSocketControllerFactory<Object>>() {
        }));
        install(new FactoryModuleBuilder().build(CheckboxInputSocketController.Factory.class));
        install(new FactoryModuleBuilder().build(ListSpinnerInputSocketController.Factory.class));
        install(new FactoryModuleBuilder().build(RangeInputSocketController.Factory.class));
        install(new FactoryModuleBuilder().build(new TypeLiteral<SelectInputSocketController.Factory<Object>>() {
        }));
        install(new FactoryModuleBuilder().build(NumberSpinnerInputSocketController.Factory.class));
        install(new FactoryModuleBuilder().build(SliderInputSocketController.Factory.class));
        install(new FactoryModuleBuilder().build(TextFieldInputSocketController.Factory.class));
        // END Input Socket Controller Factories

    }
}
