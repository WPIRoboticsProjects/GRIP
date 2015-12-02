package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Scalar;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.inRange;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * An {@link edu.wpi.grip.core.Operation} that converts a color image into a binary image based on the HSV threshold ranges for each channel
 */
public class HSVThresholdOperation extends ThresholdOperation {

    private final SocketHint<Mat> inputHint = new SocketHint<Mat>("Input", Mat.class, Mat::new);
    private final SocketHint<List> hueHint = new SocketHint<List>("Hue", List.class,
            () -> Arrays.asList(0.0, 180.0), SocketHint.View.RANGE, new List[]{Arrays.asList(0.0, 180.0)});
    private final SocketHint<List> saturationHint = new SocketHint<List>("Saturation", List.class,
            () -> Arrays.asList(0.0, 255.0), SocketHint.View.RANGE, new List[]{Arrays.asList(0.0, 255.0)});
    private final SocketHint<List> valueHint = new SocketHint<List>("Value", List.class,
            () -> Arrays.asList(0.0, 255.0), SocketHint.View.RANGE, new List[]{Arrays.asList(0.0, 255.0)});

    private final SocketHint<Mat> outputHint = new SocketHint<Mat>("output", Mat.class, Mat::new);

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

        final Mat input = ((InputSocket<Mat>) inputs[0]).getValue().get();
        final List<Number> channel1 = ((InputSocket<List<Number>>) inputs[1]).getValue().get();
        final List<Number> channel2 = ((InputSocket<List<Number>>) inputs[2]).getValue().get();
        final List<Number> channel3 = ((InputSocket<List<Number>>) inputs[3]).getValue().get();

        final OutputSocket<Mat> outputSocket = (OutputSocket<Mat>) outputs[0];
        final Mat output = outputSocket.getValue().get();

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

        try {
            cvtColor(input, hsv, COLOR_BGR2HSV);
            inRange(hsv, low, high, output);
            outputSocket.setValue(output);
        } catch (RuntimeException e) {
            e.printStackTrace(); // TODO: Report OpenCV errors
        }
    }
}
