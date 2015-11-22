package edu.wpi.grip.core.operations.opencv;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;

/** Operation to call {@link opencv_core#minMaxLoc} */
public class MinMaxLoc implements CVOperation {

    private final SocketHint<Mat>
            srcInputHint = new SocketHint<Mat>("Image", Mat.class, Mat::new, SocketHint.View.NONE, null),
            maskInputHint = new SocketHint<Mat>("Mask", Mat.class, Mat::new, SocketHint.View.NONE, null);

    private final SocketHint<Number>
            minValOutputHint = new SocketHint<Number>("Min Val", Number.class, 0),
            maxValOutputHint = new SocketHint<Number>("Max Val", Number.class, 0);

    private final SocketHint<Point>
            minLocOutputHint = new SocketHint<Point>("Min Loc", Point.class, Point::new, SocketHint.View.NONE, null),
            maxLocOutputHint = new SocketHint<Point>("Max Loc", Point.class, Point::new, SocketHint.View.NONE, null);

    @Override
    public String getName() {
        return "Find Min and Max";
    }

    @Override
    public String getDescription() {
        return "Find the global minimum and maximum in a single channel grayscale Mat.";
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
        final Mat src = (Mat) inputs[0].getValue();
        Mat mask = (Mat) inputs[1].getValue();
        if (mask.empty()) mask = null;
        final double minVal[] = new double [1];
        final double maxVal[] = new double [1];
        final Point minLoc = (Point) outputs[2].getValue();
        final Point maxLoc = (Point) outputs[3].getValue();

        try {
            opencv_core.minMaxLoc(src, minVal, maxVal, minLoc, maxLoc, mask);
            ((OutputSocket<Number>) outputs[0]).setValue(minVal[0]);
            ((OutputSocket<Number>) outputs[1]).setValue(maxVal[0]);
            ((OutputSocket) outputs[2]).setValue(outputs[2].getValue());
            ((OutputSocket) outputs[3]).setValue(outputs[3].getValue());
        } catch (final Exception e) {
            //TODO Add socket error parsing
            e.printStackTrace();
        }
    }
}