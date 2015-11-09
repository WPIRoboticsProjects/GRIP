package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.operations.opencv.CVOperation;
import org.bytedeco.javacpp.opencv_core;

import java.util.Arrays;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.inRange;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * An {@link edu.wpi.grip.core.Operation} that converts a color image into a binary image based on the HSV threshold ranges for each channel
 */
public class HSVThresholdOperation implements CVOperation{

    private final SocketHint<opencv_core.Mat> inputHint = new SocketHint<opencv_core.Mat>("Input", opencv_core.Mat.class, opencv_core.Mat::new);
    private final SocketHint<List> hueHint = new SocketHint<List>("Hue", List.class,
            () -> Arrays.asList(0.0, 180.0), SocketHint.View.RANGE, new List[]{Arrays.asList(0.0, 180.0)});
    private final SocketHint<List> saturationHint = new SocketHint<List>("Saturation", List.class,
            () -> Arrays.asList(0.0, 255.0), SocketHint.View.RANGE, new List[]{Arrays.asList(0.0, 255.0)});
    private final SocketHint<List> valueHint = new SocketHint<List>("Value", List.class,
            () -> Arrays.asList(0.0, 255.0), SocketHint.View.RANGE, new List[]{Arrays.asList(0.0, 255.0)});

    private final SocketHint<opencv_core.Mat> outputHint = new SocketHint<opencv_core.Mat>("output", opencv_core.Mat.class, opencv_core.Mat::new);

    @Override
    public String getName() {
        return "HSV Threshold";
    }

    @Override
    public String getDescription() {
        return "Segment an image based on hue, saturation and value ranges";
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
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final opencv_core.Mat input = ((InputSocket<opencv_core.Mat>) inputs[0]).getValue();
        final List<Number> channel1 = ((InputSocket<List<Number>>) inputs[1]).getValue();
        final List<Number> channel2 = ((InputSocket<List<Number>>) inputs[2]).getValue();
        final List<Number> channel3 = ((InputSocket<List<Number>>) inputs[3]).getValue();

        final OutputSocket<opencv_core.Mat> outputSocket = (OutputSocket<opencv_core.Mat>) outputs[0];
        final opencv_core.Mat output = outputSocket.getValue();

        // Do nothing if nothing is connected to the input
        // TODO: this should happen automatically for all sockets that are marked as required
        if (input.empty()) {
            outputSocket.setValue(outputSocket.getSocketHint().createInitialValue());
            return;
        }

        final opencv_core.Mat low = new opencv_core.Mat(input.size(), input.type(), new opencv_core.Scalar(
                channel1.get(0).doubleValue(),
                channel2.get(0).doubleValue(),
                channel3.get(0).doubleValue(), 0));

        final opencv_core.Mat high = new opencv_core.Mat(input.size(), input.type(), new opencv_core.Scalar(
                channel1.get(1).doubleValue(),
                channel2.get(1).doubleValue(),
                channel3.get(1).doubleValue(), 0));

        final opencv_core.Mat hsv = new opencv_core.Mat();
        cvtColor(input, hsv, COLOR_BGR2HSV);
        inRange(hsv, low, high, output);
        outputSocket.setValue(output);
    }
}
