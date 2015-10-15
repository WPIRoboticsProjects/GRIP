package edu.wpi.grip.core.operations.opencv;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_core.Size;

public class NewSizeOperation implements CVOperation {

    private final SocketHint<Number> widthHint = new SocketHint<Number>("width", Number.class, -1, SocketHint.View.SPINNER, new Integer[]{-1, Integer.MAX_VALUE}, false);
    private final SocketHint<Number> heightHint = new SocketHint<Number>("height", Number.class, -1, SocketHint.View.SPINNER, new Integer[]{-1, Integer.MAX_VALUE}, false);
    private final SocketHint<Size> outputHint = new SocketHint<Size>("size", Size.class, Size::new);

    @Override
    public String getName() {
        return "New Size";
    }

    @Override
    public String getDescription() {
        return "Create a Size";
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[]{new InputSocket(eventBus, widthHint), new InputSocket(eventBus, heightHint)};
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[]{new OutputSocket(eventBus, outputHint)};
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final InputSocket<Number> widthSocket = (InputSocket<Number>) inputs[0];
        final InputSocket<Number> heightSocket = (InputSocket<Number>) inputs[1];
        final int widthValue = widthSocket.getValue().intValue();
        final int heightValue = heightSocket.getValue().intValue();
        final OutputSocket<Size> outputSocket = (OutputSocket<Size>) outputs[0];
        outputSocket.setValue(new Size(widthValue, heightValue));
    }
}

