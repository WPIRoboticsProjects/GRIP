package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;


/**
 * Performs the opencv add operation
 */
public class AddOperation implements Operation {
    private SocketHint<Mat>
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
                new InputSocket<Mat>(eventBus, aHint),
                new InputSocket<Mat>(eventBus, bHint)
        };
    }

    @Override
    public OutputSocket[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[]{
                new OutputSocket<Mat>(eventBus, sumHint)
        };
    }

    @Override
    public void perform(InputSocket[] inputs, OutputSocket[] outputs) {
        Socket<Mat> a = inputs[0], b = inputs[1], sum = outputs[0];
        opencv_core.add(a.getValue().get(), b.getValue().get(), sum.getValue().get());
    }
}
