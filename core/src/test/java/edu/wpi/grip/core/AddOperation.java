package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.*;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;


/**
 * Performs the opencv add operation
 */
public class AddOperation implements Operation {
    private final SocketHint<Mat>
            aHint = SocketHints.Inputs.createMatSocketHint("a", false),
            bHint = SocketHints.Inputs.createMatSocketHint("b", false),
            sumHint = SocketHints.Inputs.createMatSocketHint("sum", true);

    @Override
    public String getName() {
        return "OpenCV Add";
    }

    @Override
    public String getDescription() {
        return "Compute the per-pixel sum of two images.";
    }

    @Override
    public InputSocket[] createInputSockets(EventBus eventBus) {
        return new InputSocket[]{
                new InputSocket<>(eventBus, aHint),
                new InputSocket<>(eventBus, bHint)
        };
    }

    @Override
    public OutputSocket[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[]{
                new OutputSocket<>(eventBus, sumHint)
        };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Socket<Mat>
                a = aHint.safeCastSocket(inputs[0]),
                b = bHint.safeCastSocket(inputs[1]),
                sum = sumHint.safeCastSocket(outputs[0]);
        opencv_core.add(aHint.retrieveValue(a), bHint.retrieveValue(b), sumHint.retrieveValue(sum));
    }
}
