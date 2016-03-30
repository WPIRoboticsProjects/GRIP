package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

public class AdditionOperation implements Operation {
    private SocketHint<Number>
            aHint = SocketHints.createNumberSocketHint("a", 0.0),
            bHint = SocketHints.createNumberSocketHint("b", 0.0),
            cHint = SocketHints.Outputs.createNumberSocketHint("c", 0.0);

    @Override
    public String getName() {
        return "Add";
    }

    @Override
    public String getDescription() {
        return "Compute the sum of two doubles";
    }

    @Override
    public InputSocket[] createInputSockets(EventBus eventBus) {
        return new InputSocket[]{new InputSocket<>(eventBus, aHint), new InputSocket<>(eventBus, bHint)};
    }

    @Override
    public OutputSocket[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[]{new OutputSocket<>(eventBus, cHint)};
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Number a = aHint.retrieveValue(inputs[0]), b = bHint.retrieveValue(inputs[1]);

        cHint.safeCastSocket(outputs[0]).setValue(a.doubleValue() + b.doubleValue());
    }
}
