package edu.wpi.grip.core.operations.composite;


import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Scalar;
import static org.bytedeco.javacpp.opencv_core.inRange;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2HLS;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * An {@link Operation} that converts a color image into a binary image based on the HSL threshold ranges
 */
public class HSLThresholdOperation extends ThresholdOperation {

    public static final OperationDescription DESCRIPTION =
            OperationDescription.builder()
                    .name("HSL Threshold")
                    .summary("Segment an image based on hue, saturation, and luminance ranges.")
                    .category(OperationDescription.Category.IMAGE_PROCESSING)
                    .icon(Icon.iconStream("threshold"))
                    .build();

    private static final Logger logger = Logger.getLogger(HSLThresholdOperation.class.getName());
    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<List<Number>> hueHint = SocketHints.Inputs.createNumberListRangeSocketHint("Hue", 0.0, 180.0);
    private final SocketHint<List<Number>> saturationHint = SocketHints.Inputs.createNumberListRangeSocketHint("Saturation", 0.0, 255.0);
    private final SocketHint<List<Number>> luminanceHint = SocketHints.Inputs.createNumberListRangeSocketHint("Luminance", 0.0, 255.0);

    private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");

    private final InputSocket<Mat> inputSocket;
    private final InputSocket<List<Number>> hueSocket;
    private final InputSocket<List<Number>> saturationSocket;
    private final InputSocket<List<Number>> luminanceSocket;

    private final OutputSocket<Mat> outputSocket;

    public HSLThresholdOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory outputSocketFactory) {
        this.inputSocket = inputSocketFactory.create(inputHint);
        this.hueSocket = inputSocketFactory.create(hueHint);
        this.saturationSocket = inputSocketFactory.create(saturationHint);
        this.luminanceSocket = inputSocketFactory.create(luminanceHint);

        this.outputSocket = outputSocketFactory.create(outputHint);
    }

    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.of(
                inputSocket,
                hueSocket,
                saturationSocket,
                luminanceSocket
        );
    }

    @Override
    public List<OutputSocket> getOutputSockets() {
        return ImmutableList.of(
                outputSocket
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform() {
        final Mat input = inputSocket.getValue().get();
        final List<Number> channel1 = hueSocket.getValue().get();
        final List<Number> channel2 = saturationSocket.getValue().get();
        final List<Number> channel3 = luminanceSocket.getValue().get();

        if (input.channels() != 3) {
            throw new IllegalArgumentException("HSL Threshold needs a 3-channel input");
        }

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
