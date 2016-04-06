package edu.wpi.grip.core.operations.composite;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.sockets.*;

import java.util.Optional;

public class ValveOperation implements Operation {
    // This hint toggles the switch between using the true and false sockets
    private final SocketHint<Boolean> valveHint = SocketHints.createBooleanSocketHint("valve", true);
    @Override
    public String getName() {
        return "Valve";
    }

    @Override
    public String getDescription() {
        return "Toggle an output socket on or off using a boolean";
    }

    @Override
    public Category getCategory() {
        return Category.LOGICAL;
    }

    @Override
    public SocketsProvider createSockets(EventBus eventBus) {
        final LinkedSocketHint linkedSocketHint = new LinkedSocketHint(eventBus);
        final InputSocket<?>[] inputs = new InputSocket[]{
                new InputSocket<>(eventBus, valveHint),
                linkedSocketHint.linkedInputSocket("Input"),
        };
        final OutputSocket<?>[] outputs = new OutputSocket[]{
                linkedSocketHint.linkedOutputSocket("Output")
        };
        return new SocketsProvider(inputs, outputs);
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        throw new UnsupportedOperationException("This method should not be used");
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        throw new UnsupportedOperationException("This method should not be used");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final boolean valveValue = valveHint.retrieveValue(inputs[0]);
        // If the input is true pass the value through
        if (valveValue) {
            outputs[0].setValueOptional(((InputSocket) inputs[1]).getValue());
        } else {
            outputs[0].setValueOptional((Optional) Optional.empty());
        }
    }
}
