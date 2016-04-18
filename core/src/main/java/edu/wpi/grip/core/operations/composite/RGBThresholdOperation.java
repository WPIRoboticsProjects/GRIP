package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * An {@link Operation} that converts a color image into a binary image based on threshold ranges for each channel
 */
public class RGBThresholdOperation extends ThresholdOperation {

    private static final Logger logger = Logger.getLogger(RGBThresholdOperation.class.getName());
    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<List> redHint = SocketHints.Inputs.createNumberListRangeSocketHint("Red", 0.0, 1.0);
    private final SocketHint<List> greenHint = SocketHints.Inputs.createNumberListRangeSocketHint("Green", 0.0, 1.0);
    private final SocketHint<List> blueHint = SocketHints.Inputs.createNumberListRangeSocketHint("Blue", 0.0, 1.0);

    private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");

    @Override
    public String getName() {
        return "RGB Threshold";
    }

    @Override
    public String getDescription() {
        return "Segment an image based on color ranges.";
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, inputHint),
                new InputSocket<>(eventBus, redHint),
                new InputSocket<>(eventBus, greenHint),
                new InputSocket<>(eventBus, blueHint),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{
                new OutputSocket<>(eventBus, outputHint)
        };
    }

    @Override
    public Optional<Mat[]> createData() {
        return Optional.of(new Mat[]{new Mat(), new Mat()});
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        final Mat[] dataArray = (Mat[]) data.orElseThrow(() -> new IllegalStateException("Data was not provided"));

        final Mat input = ((InputSocket<Mat>) inputs[0]).getValue().get();
        final List<Number> channel1 = ((InputSocket<List<Number>>) inputs[1]).getValue().get();
        final List<Number> channel2 = ((InputSocket<List<Number>>) inputs[2]).getValue().get();
        final List<Number> channel3 = ((InputSocket<List<Number>>) inputs[3]).getValue().get();

        if (input.channels() != 3) {
            throw new IllegalArgumentException("RGB Threshold needs a 3-channel input");
        }

        if (input.depth() < CV_8U || input.depth() > CV_32F) {
            throw new UnsupportedOperationException("RGB Threshold only works with 8-bit, 16-bit, and 32-bit images");
        }

        // Map the image type to bit depth (2^8, 2^16, or 2^32)
        // Takes advantage of CV_8U/S, CV_16U/S, and CV_32S/F being 0, 1, 2, 3, 4, 5, respectively
        final long depth = (1L << (8 << (input.depth() / 2))) - 1;

        final OutputSocket<Mat> outputSocket = (OutputSocket<Mat>) outputs[0];
        final Mat output = outputSocket.getValue().get();


        final Scalar lowScalar = new Scalar(
                channel3.get(0).doubleValue() * depth,
                channel2.get(0).doubleValue() * depth,
                channel1.get(0).doubleValue() * depth, 0);

        final Scalar highScalar = new Scalar(
                channel3.get(1).doubleValue() * depth,
                channel2.get(1).doubleValue() * depth,
                channel1.get(1).doubleValue() * depth, 0);

        final Mat low = reallocateMatIfInputSizeOrWidthChanged(dataArray, 0, lowScalar, input);
        final Mat high = reallocateMatIfInputSizeOrWidthChanged(dataArray, 1, highScalar, input);

        try {
            inRange(input, low, high, output);

            outputSocket.setValue(output);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
