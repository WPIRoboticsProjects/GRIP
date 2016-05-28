package edu.wpi.grip.core.operations.composite;

import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Scalar;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bytedeco.javacpp.opencv_core.inRange;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * An {@link Operation} that converts a color image into a binary image based on the HSV threshold ranges for each channel
 */
public class HSVThresholdOperation extends ThresholdOperation {

    public static final OperationDescription DESCRIPTION =
            OperationDescription.builder()
                    .name("HSV Threshold")
                    .summary("Segment an image based on hue, saturation, and value ranges.")
                    .category(OperationDescription.Category.IMAGE_PROCESSING)
                    .icon(Icon.iconStream("threshold"))
                    .build();

    private static final Logger logger = Logger.getLogger(HSVThresholdOperation.class.getName());
    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<List<Number>> hueHint = SocketHints.Inputs.createNumberListRangeSocketHint("Hue", 0.0, 180.0);
    private final SocketHint<List<Number>> saturationHint = SocketHints.Inputs.createNumberListRangeSocketHint("Saturation", 0.0, 255.0);
    private final SocketHint<List<Number>> valueHint = SocketHints.Inputs.createNumberListRangeSocketHint("Value", 0.0, 255.0);

    private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");

    private final InputSocket<Mat> inputSocket;
    private final InputSocket<List<Number>> hueSocket;
    private final InputSocket<List<Number>> saturationSocket;
    private final InputSocket<List<Number>> valueSocket;

    private final OutputSocket<Mat> outputSocket;

    public HSVThresholdOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory outputSocketFactory) {
        this.inputSocket = inputSocketFactory.create(inputHint);
        this.hueSocket = inputSocketFactory.create(hueHint);
        this.saturationSocket = inputSocketFactory.create(saturationHint);
        this.valueSocket = inputSocketFactory.create(valueHint);

        this.outputSocket = outputSocketFactory.create(outputHint);
    }

    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.of(
                inputSocket,
                hueSocket,
                saturationSocket,
                valueSocket
        );
    }

    @Override
    public List<OutputSocket> getOutputSockets() {
        return ImmutableList.of(
                outputSocket
        );
    }


    @Override
    public void perform() {
        final Mat input = inputSocket.getValue().get();
        final List<Number> channel1 = hueSocket.getValue().get();
        final List<Number> channel2 = saturationSocket.getValue().get();
        final List<Number> channel3 = valueSocket.getValue().get();

        if (input.channels() != 3) {
            throw new IllegalArgumentException("HSV Threshold needs a 3-channel input");
        }

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
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
