package edu.wpi.grip.core.operations.composite;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.sockets.*;

/**
 * Allows for switching between two arbitrary typed {@link Socket} using a
 * boolean {@link InputSocket}
 */
public class SwitchOperation implements Operation {
    private final SocketHint<Boolean> switcherHint = SocketHints.createBooleanSocketHint("switch", true);

    @Override
    public String getName() {
        return "Switch";
    }

    @Override
    public String getDescription() {
        return "Switch between two possible input sockets using a boolean";
    }

    @Override
    public Category getCategory() {
        return Category.LOGICAL;
    }

    @Override
    public SocketsProvider createSockets(EventBus eventBus) {
        // This hint toggles the switch between using the true and false sockets


        final LinkedSocketHint linkedSocketHint = new LinkedSocketHint(eventBus);
        final InputSocket<?>[] inputs = new InputSocket[]{
                new InputSocket<>(eventBus, switcherHint),
                linkedSocketHint.linkedInputSocket("If True"),
                linkedSocketHint.linkedInputSocket("If False")
        };
        final OutputSocket<?>[] outputs = new OutputSocket[]{
                linkedSocketHint.linkedOutputSocket("Result")
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
        final boolean switchVal =  switcherHint.retrieveValue(inputs[0]);
        // If the input is true pass one value through
        if (switchVal) {
            outputs[0].setValueOptional(((InputSocket) inputs[1]).getValue());
        } else { // Otherwise pass the other one through
            outputs[0].setValueOptional(((InputSocket) inputs[2]).getValue());
        }
    }
}
