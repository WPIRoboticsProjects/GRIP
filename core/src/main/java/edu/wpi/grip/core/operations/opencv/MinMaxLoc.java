package edu.wpi.grip.core.operations.opencv;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.SocketHints;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Operation to call {@link opencv_core#minMaxLoc} */
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
    private static Logger logger =  Logger.getLogger(MinMaxLoc.class.getName());

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
        return new InputSocket[] { new InputSocket(eventBus, srcInputHint), new InputSocket(eventBus, maskInputHint) };
    }

    @Override
    @SuppressWarnings("unchecked")
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[] {
                new OutputSocket(eventBus, minValOutputHint),
                new OutputSocket(eventBus, maxValOutputHint),
                new OutputSocket(eventBus, minLocOutputHint),
                new OutputSocket(eventBus, maxLocOutputHint),
        };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat src = (Mat) inputs[0].getValue().get();
        Mat mask = (Mat) inputs[1].getValue().get();
        if (mask.empty()) mask = null;
        final double minVal[] = new double [1];
        final double maxVal[] = new double [1];
        final Point minLoc = (Point) outputs[2].getValue().get();
        final Point maxLoc = (Point) outputs[3].getValue().get();

        try {
            opencv_core.minMaxLoc(src, minVal, maxVal, minLoc, maxLoc, mask);
            ((OutputSocket<Number>) outputs[0]).setValue(minVal[0]);
            ((OutputSocket<Number>) outputs[1]).setValue(maxVal[0]);
            ((OutputSocket) outputs[2]).setValue(outputs[2].getValue().get());
            ((OutputSocket) outputs[3]).setValue(outputs[3].getValue().get());
        } catch (final Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
}