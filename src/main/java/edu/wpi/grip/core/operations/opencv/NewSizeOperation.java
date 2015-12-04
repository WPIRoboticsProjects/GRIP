package edu.wpi.grip.core.operations.opencv;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.SocketHints;
import org.bytedeco.javacpp.opencv_core.Size;

import java.io.InputStream;
import java.util.Optional;

public class NewSizeOperation implements CVOperation {

    private final SocketHint<Number> widthHint = SocketHints.Inputs.createNumberSpinnerSocketHint("width", -1, -1, Integer.MAX_VALUE);
    private final SocketHint<Number> heightHint = SocketHints.Inputs.createNumberSpinnerSocketHint("height", -1, -1, Integer.MAX_VALUE);
    private final SocketHint<Size> outputHint = SocketHints.Outputs.createSizeSocketHint("size");

    @Override
    public String getName() {
        return "New Size";
    }

    @Override
    public String getDescription() {
        return "Create a size.";
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/size.png"));
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
        final int widthValue = widthSocket.getValue().get().intValue();
        final int heightValue = heightSocket.getValue().get().intValue();
        final OutputSocket<Size> outputSocket = (OutputSocket<Size>) outputs[0];
        outputSocket.setValue(new Size(widthValue, heightValue));
    }
}

