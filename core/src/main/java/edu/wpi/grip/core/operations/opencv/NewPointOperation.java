package edu.wpi.grip.core.operations.opencv;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.*;
import org.bytedeco.javacpp.opencv_core.Point;

import java.io.InputStream;
import java.util.Optional;

public class NewPointOperation implements CVOperation {

    private final SocketHint<Number> xHint = SocketHints.Inputs.createNumberSpinnerSocketHint("x", -1,
            Integer.MIN_VALUE, Integer.MAX_VALUE);
    private final SocketHint<Number> yHint = SocketHints.Inputs.createNumberSpinnerSocketHint("y", -1,
            Integer.MIN_VALUE, Integer.MAX_VALUE);
    private final SocketHint<Point> outputHint = SocketHints.Outputs.createPointSocketHint("point");

    @Override
    public String getName() {
        return "New Point";
    }

    @Override
    public String getDescription() {
        return "Create a point by (x,y) value.";
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/point.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[]{new InputSocket<>(eventBus, xHint), new InputSocket<>(eventBus, yHint)};
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[]{new OutputSocket<>(eventBus, outputHint)};
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final int xValue = xHint.retrieveValue(inputs[0]).intValue();
        final int yValue = yHint.retrieveValue(inputs[1]).intValue();
        final Socket<Point> outputSocket = outputHint.safeCastSocket(outputs[0]);
        outputSocket.setValue(new Point(xValue, yValue));
    }
}
