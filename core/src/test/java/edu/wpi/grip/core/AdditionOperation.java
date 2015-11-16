package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;

public class AdditionOperation implements Operation {
    private SocketHint<Double>
            aHint = new SocketHint<>("a", Double.class, 0.0),
            bHint = new SocketHint<>("b", Double.class, 0.0),
            cHint = new SocketHint<>("c", Double.class, 0.0);

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
    public void perform(InputSocket[] inputs, OutputSocket[] outputs) {
        InputSocket<Double> a = inputs[0], b = inputs[1];
        OutputSocket<Double> c = outputs[0];

        c.setValue(a.getValue() + b.getValue());
    }
}
