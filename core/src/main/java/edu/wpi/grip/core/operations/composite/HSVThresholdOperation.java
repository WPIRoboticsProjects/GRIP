package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.*;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Scalar;

import java.util.List;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.inRange;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * An {@link edu.wpi.grip.core.Operation} that converts a color image into a binary image based on the HSV threshold ranges for each channel
 */
public class HSVThresholdOperation extends ThresholdOperation {
    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<List<Number>> hueHint = SocketHints.Inputs.createNumberListRangeSocketHint("Hue", 0.0, 180.0);
    private final SocketHint<List<Number>> saturationHint = SocketHints.Inputs.createNumberListRangeSocketHint("Saturation", 0.0, 255.0);
    private final SocketHint<List<Number>> valueHint = SocketHints.Inputs.createNumberListRangeSocketHint("Value", 0.0, 255.0);

    private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");

    @Override
    public String getName() {
        return "HSV Threshold";
    }

    @Override
    public String getDescription() {
        return "Segment an image based on hue, saturation and value ranges.";
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, inputHint),
                new InputSocket<>(eventBus, hueHint),
                new InputSocket<>(eventBus, saturationHint),
                new InputSocket<>(eventBus, valueHint),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{
                new OutputSocket<>(eventBus, outputHint)
        };
    }


    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        final Mat[] dataArray = (Mat[]) data.orElseThrow(() -> new IllegalStateException("Data was not provided"));

        final Mat input = inputHint.retrieveValue(inputs[0]);
        final List<Number> channel1 = hueHint.retrieveValue(inputs[1]);
        final List<Number> channel2 = saturationHint.retrieveValue(inputs[2]);
        final List<Number> channel3 = valueHint.retrieveValue(inputs[3]);

        if (input.channels() != 3) {
            throw new IllegalArgumentException("HSV Threshold needs a 3-channel input");
        }

        final Socket<Mat> outputSocket = outputHint.safeCastSocket(outputs[0]);
        final Mat output = outputHint.retrieveValue(outputSocket);

        final Scalar lowScalar = new Scalar(
                channel1.get(0).doubleValue(),
                channel2.get(0).doubleValue(),
                channel3.get(0).doubleValue(), 0);
        final Scalar highScalar = new Scalar(
                channel1.get(1).doubleValue(),
                channel2.get(1).doubleValue(),
                channel3.get(1).doubleValue(), 0);

        final Mat low = reallocateMatIfInputSizeOrWidthChanged(dataArray, 0, lowScalar, input);
        final Mat high = reallocateMatIfInputSizeOrWidthChanged(dataArray, 1, highScalar, input);
        final Mat hsv = dataArray[2];


        cvtColor(input, hsv, COLOR_BGR2HSV);
        inRange(hsv, low, high, output);
        outputSocket.setValue(output);
    }
}
