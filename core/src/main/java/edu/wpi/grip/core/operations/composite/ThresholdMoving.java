package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHints;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;

import java.util.Optional;

import static edu.wpi.grip.core.Operation.Category.IMAGE_PROCESSING;

/**
 * Finds the absolute difference between the current image and the previous image.
 */
public class ThresholdMoving implements Operation {


    @Override
    public String getName() {
        return "Threshold Moving";
    }

    @Override
    public String getDescription() {
        return "Thresholds off parts of the image that have moved or changed between the previous and next image.";
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, SocketHints.Inputs.createMatSocketHint("image", false))
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{
                new OutputSocket<>(eventBus, SocketHints.Outputs.createMatSocketHint("moved"))
        };
    }

    @Override
    public Category getCategory() {
        return IMAGE_PROCESSING;
    }

    @Override
    public Optional<?> createData() {
        return Optional.of(new Mat());
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        final Mat input = ((InputSocket<Mat>) inputs[0]).getValue().get();
        final OutputSocket<Mat> outputSocket = (OutputSocket<Mat>) outputs[0];
        final Mat lastImage = (Mat) data.get();

        final Size lastSize = lastImage.size();
        final Size inputSize = input.size();
        if (!lastImage.empty() && lastSize.height() == inputSize.height() && lastSize.width() == inputSize.width()) {
            opencv_core.absdiff(input, lastImage, outputSocket.getValue().get());
        }
        input.copyTo(lastImage);
        outputSocket.setValue(outputSocket.getValue().get());
    }
}
