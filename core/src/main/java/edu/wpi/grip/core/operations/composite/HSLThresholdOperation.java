package edu.wpi.grip.core.operations.composite;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.SocketHints;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2HLS;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * An {@link edu.wpi.grip.core.Operation} that converts a color image into a binary image based on the HSL threshold ranges
 */
public class HSLThresholdOperation extends ThresholdOperation {
    private static final Logger logger = Logger.getLogger(HSLThresholdOperation.class.getName());
    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<List> hueHint = SocketHints.Inputs.createNumberListRangeSocketHint("Hue", 0.0, 180.0);
    private final SocketHint<List> saturationHint = SocketHints.Inputs.createNumberListRangeSocketHint("Saturation", 0.0, 255.0);
    private final SocketHint<List> luminanceHint = SocketHints.Inputs.createNumberListRangeSocketHint("Luminance", 0.0, 255.0);

    private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");

    @Override
    public String getName() {
        return "HSL Threshold";
    }

    @Override
    public String getDescription() {
        return "Segment an image based on hue, saturation, and luminance ranges.";
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, inputHint),
                new InputSocket<>(eventBus, hueHint),
                new InputSocket<>(eventBus, saturationHint),
                new InputSocket<>(eventBus, luminanceHint),
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

        final Mat input = ((InputSocket<Mat>) inputs[0]).getValue().get();
        final List<Number> channel1 = ((InputSocket<List<Number>>) inputs[1]).getValue().get();
        final List<Number> channel2 = ((InputSocket<List<Number>>) inputs[2]).getValue().get();
        final List<Number> channel3 = ((InputSocket<List<Number>>) inputs[3]).getValue().get();

        if (input.channels() != 3) {
            throw new IllegalArgumentException("HSL Threshold needs a 3-channel input");
        }

        final OutputSocket<Mat> outputSocket = (OutputSocket<Mat>) outputs[0];
        final Mat output = outputSocket.getValue().get();

        // Intentionally 1, 3, 2. This maps to the HLS open cv expects
        final Scalar lowScalar = new Scalar(
                channel1.get(0).doubleValue(),
                channel3.get(0).doubleValue(),
                channel2.get(0).doubleValue(), 0);

        final Scalar highScalar = new Scalar(
                channel1.get(1).doubleValue(),
                channel3.get(1).doubleValue(),
                channel2.get(1).doubleValue(), 0);

        final Mat low = reallocateMatIfInputSizeOrWidthChanged(dataArray, 0, lowScalar, input);
        final Mat high = reallocateMatIfInputSizeOrWidthChanged(dataArray, 1, highScalar, input);
        final Mat hls = dataArray[2];

        try {
            cvtColor(input, hls, COLOR_BGR2HLS);
            inRange(hls, low, high, output);
            outputSocket.setValue(output);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
