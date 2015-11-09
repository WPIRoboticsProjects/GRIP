package edu.wpi.grip.core.operations.composite;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.operations.opencv.CVOperation;

import java.util.Arrays;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2HLS;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * An {@link edu.wpi.grip.core.Operation} that converts a color image into a binary image based on the HSL threshold ranges
 */
public class HSLThresholdOperation implements CVOperation {
    private final SocketHint<Mat> inputHint = new SocketHint<Mat>("Input", Mat.class, Mat::new);
    private final SocketHint<List> hueHint = new SocketHint<List>("Hue", List.class,
            () -> Arrays.asList(0.0, 180.00), SocketHint.View.RANGE, new List[]{Arrays.asList(0.0, 180.0)});
    private final SocketHint<List> saturationHint = new SocketHint<List>("Saturation", List.class,
            () -> Arrays.asList(0.0, 255.0), SocketHint.View.RANGE, new List[]{Arrays.asList(0.0, 255.0)});
    private final SocketHint<List> luminanceHint = new SocketHint<List>("Luminance", List.class,
            () -> Arrays.asList(0.0, 255.0), SocketHint.View.RANGE, new List[]{Arrays.asList(0.0, 255.0)});

    private final SocketHint<Mat> outputHint = new SocketHint<Mat>("output", Mat.class, Mat::new);

    @Override
    public String getName() {
        return "HSL Threshold";
    }

    @Override
    public String getDescription() {
        return "Segment an image based on hue, saturation, and luminance ranges";
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
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat input = ((InputSocket<Mat>) inputs[0]).getValue();
        final List<Number> channel1 = ((InputSocket<List<Number>>) inputs[1]).getValue();
        final List<Number> channel2 = ((InputSocket<List<Number>>) inputs[2]).getValue();
        final List<Number> channel3 = ((InputSocket<List<Number>>) inputs[3]).getValue();

        final OutputSocket<Mat> outputSocket = (OutputSocket<Mat>) outputs[0];
        final Mat output = outputSocket.getValue();

        // Do nothing if nothing is connected to the input
        // TODO: this should happen automatically for all sockets that are marked as required
        if (input.empty()) {
            outputSocket.setValue(outputSocket.getSocketHint().createInitialValue());
            return;
        }

        // Intentionally 1, 3, 2. This maps to the HLS open cv expects
        final Mat low = new Mat(input.size(), input.type(), new Scalar(
                channel1.get(0).doubleValue(),
                channel3.get(0).doubleValue(),
                channel2.get(0).doubleValue(), 0));

        final Mat high = new Mat(input.size(), input.type(), new Scalar(
                channel1.get(1).doubleValue(),
                channel3.get(1).doubleValue(),
                channel2.get(1).doubleValue(), 0));

        final Mat hls = new Mat();
        cvtColor(input, hls, COLOR_BGR2HLS);
        inRange(hls, low, high, output);
        outputSocket.setValue(output);
    }
}
