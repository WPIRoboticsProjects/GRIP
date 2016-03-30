package edu.wpi.grip.core.operations.opencv;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;

/**
 * Operation to call {@link opencv_core#minMaxLoc}
 */
public class MinMaxLoc implements CVOperation {

    private final SocketHint<Mat>
            srcInputHint = SocketHints.Inputs.createMatSocketHint("Image", false),
            maskInputHint = SocketHints.Inputs.createMatSocketHint("Mask", true);

    private final SocketHint<Number>
            minValOutputHint = SocketHints.Outputs.createNumberSocketHint("Min Val", 0),
            maxValOutputHint = SocketHints.Outputs.createNumberSocketHint("Max Val", 0);

    private final SocketHint<Point>
            minLocOutputHint = SocketHints.Outputs.createPointSocketHint("Min Loc"),
            maxLocOutputHint = SocketHints.Outputs.createPointSocketHint("Max Loc");

    @Override
    public String getName() {
        return "Find Min and Max";
    }

    @Override
    public String getDescription() {
        return "Find the global minimum and maximum in a single channel grayscale image.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[]{
                new InputSocket<>(eventBus, srcInputHint),
                new InputSocket<>(eventBus, maskInputHint)
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[]{
                new OutputSocket<>(eventBus, minValOutputHint),
                new OutputSocket<>(eventBus, maxValOutputHint),
                new OutputSocket<>(eventBus, minLocOutputHint),
                new OutputSocket<>(eventBus, maxLocOutputHint),
        };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat src = srcInputHint.retrieveValue(inputs[0]);
        Mat mask = maskInputHint.retrieveValue(inputs[1]);
        if (mask.empty()) mask = null;
        final double minVal[] = new double[1];
        final double maxVal[] = new double[1];
        final Point minLoc = minLocOutputHint.retrieveValue(outputs[2]);
        final Point maxLoc = maxLocOutputHint.retrieveValue(outputs[3]);

        opencv_core.minMaxLoc(src, minVal, maxVal, minLoc, maxLoc, mask);
        minValOutputHint.safeCastSocket(outputs[0]).setValue(minVal[0]);
        maxValOutputHint.safeCastSocket(outputs[1]).setValue(maxVal[0]);
        minLocOutputHint.safeCastSocket(outputs[2]).setValue(minLocOutputHint.retrieveValue(outputs[2]));
        maxLocOutputHint.safeCastSocket(outputs[3]).setValue(maxLocOutputHint.retrieveValue(outputs[3]));
    }
}