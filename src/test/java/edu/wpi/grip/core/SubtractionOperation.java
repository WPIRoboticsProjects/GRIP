package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;

public class SubtractionOperation implements Operation {
    private SocketHint<Number>
            aHint = SocketHints.createNumberSocketHint("a", 0.0),
            bHint = SocketHints.createNumberSocketHint("b", 0.0),
            cHint = SocketHints.Outputs.createNumberSocketHint("c", 0.0);

    @Override
    public String getName() {
        return "Subtract";
    }

    @Override
    public String getDescription() {
        return "Compute the difference between two doubles";
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
    public void perform(InputSocket[] inputs, OutputSocket[] outputs) {
        InputSocket<Number> a = inputs[0], b = inputs[1];
        OutputSocket<Number> c = outputs[0];

        c.setValue(a.getValue().get().doubleValue() - b.getValue().get().doubleValue());
    }
}
