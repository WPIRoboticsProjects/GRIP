package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;

import java.util.Optional;

public class MockStep extends Step {

    public MockStep(EventBus eventBus, @Assisted Operation operation) {
        super(eventBus, operation);
    }

    public MockStep() {
        super(new EventBus(), new Operation() {
            @Override
            public String getName() {
                return "Mock Operation";
            }

            @Override
            public String getDescription() {
                return "A mock operation description";
            }

            @Override
            public InputSocket<?>[] createInputSockets(EventBus eventBus) {
                return new InputSocket<?>[0];
            }

            @Override
            public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
                return new OutputSocket<?>[0];
            }

            @Override
            public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data){

            }
        });
    }
}
