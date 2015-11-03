package edu.wpi.grip.core.operations.opencv;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_core.Point;

public class NewPointOperation implements CVOperation {

    private final SocketHint<Number> xHint = new SocketHint<Number>("x", Number.class, -1, SocketHint.View.SPINNER, new Integer[]{Integer.MIN_VALUE, Integer.MAX_VALUE}, false);
    private final SocketHint<Number> yHint = new SocketHint<Number>("y", Number.class, -1, SocketHint.View.SPINNER, new Integer[]{Integer.MIN_VALUE, Integer.MAX_VALUE}, false);
    private final SocketHint<Point> outputHint = new SocketHint<Point>("point", Point.class, Point::new);

    @Override
    public String getName() {
        return "New Point";
    }

    @Override
    public String getDescription() {
        return "Create a Point";
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[]{new InputSocket(eventBus, xHint), new InputSocket(eventBus, yHint)};
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[]{new OutputSocket(eventBus, outputHint)};
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final InputSocket<Number> xSocket = (InputSocket<Number>) inputs[0];
        final InputSocket<Number> ySocket = (InputSocket<Number>) inputs[1];
        final int xValue = xSocket.getValue().intValue();
        final int yValue = ySocket.getValue().intValue();
        final OutputSocket<Point> outputSocket = (OutputSocket<Point>) outputs[0];
        outputSocket.setValue(new Point(xValue, yValue));
    }
}
