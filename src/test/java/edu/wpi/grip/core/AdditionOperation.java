package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;

public class AdditionOperation implements Operation {
    final private SocketHint<Double>
            aHint = new SocketHint<>("a", Double.class),
            bHint = new SocketHint<>("b", Double.class),
            cHint = new SocketHint<>("sum", Double.class);

    @Override
    public Socket[] createInputSockets(EventBus eventBus) {
        return new Socket[]{new Socket<>(eventBus, aHint), new Socket<>(eventBus, bHint)};
    }

    @Override
    public Socket[] createOutputSockets(EventBus eventBus) {
        return new Socket[]{new Socket<>(eventBus, cHint)};
    }

    @Override
    public void perform(Socket[] inputs, Socket[] outputs) {
        Socket<Double> term1 = inputs[0], term2 = inputs[1], sum = outputs[0];

        sum.setValue(term1.getValue() + term2.getValue());
    }
}
