package edu.wpi.grip.generated.opencv_imgproc;

import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_imgproc;
import edu.wpi.grip.core.operations.opencv.CVOperation;
import static org.bytedeco.javacpp.opencv_core.*;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.generated.opencv_core.enumeration.BorderTypesEnum;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * ===== THIS CODE HAS BEEN DYNAMICALLY GENERATED! DO NOT MODIFY! ==== *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
/** Operation to call {@link opencv_imgproc#erode} */
public class Erode implements CVOperation {

    private final SocketHint<Mat> srcInputHint = new SocketHint.Builder(Mat.class).identifier("src").initialValue(null).view(SocketHint.View.NONE).build(), kernelInputHint = new SocketHint.Builder(Mat.class).identifier("kernel").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<Point> anchorInputHint = new SocketHint.Builder(Point.class).identifier("anchor").initialValueSupplier(Point::new).view(SocketHint.View.NONE).build();

    private final SocketHint<BorderTypesEnum> borderTypeInputHint = new SocketHint.Builder(BorderTypesEnum.class).identifier("borderType").initialValue(BorderTypesEnum.BORDER_CONSTANT).view(SocketHint.View.SELECT).domain(BorderTypesEnum.values()).build();

    private final SocketHint<Number> iterationsInputHint = new SocketHint.Builder(Number.class).identifier("iterations").initialValue(1).view(SocketHint.View.SPINNER).domain(new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE }).build();

    private final SocketHint<Scalar> borderValueInputHint = new SocketHint.Builder(Scalar.class).identifier("borderValue").initialValueSupplier(Scalar::new).view(SocketHint.View.NONE).build();

    private final SocketHint<Mat> dstOutputHint = new SocketHint.Builder(Mat.class).identifier("dst").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "erode";
    }

    @Override
    public String getDescription() {
        return "Expands areas of lower values in an image.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, srcInputHint), new InputSocket(eventBus, kernelInputHint), new InputSocket(eventBus, anchorInputHint), new InputSocket(eventBus, iterationsInputHint), new InputSocket(eventBus, borderTypeInputHint), new InputSocket(eventBus, borderValueInputHint) };
    }

    @Override
    @SuppressWarnings("unchecked")
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[] { new OutputSocket(eventBus, dstOutputHint) };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat src = (Mat) inputs[0].getValue().get();
        final Mat dst = (Mat) outputs[0].getValue().get();
        final Mat kernel = (Mat) inputs[1].getValue().get();
        final Point anchor = (Point) inputs[2].getValue().get();
        final int iterations = ((Number) inputs[3].getValue().get()).intValue();
        final BorderTypesEnum borderType = (BorderTypesEnum) inputs[4].getValue().get();
        final Scalar borderValue = (Scalar) inputs[5].getValue().get();
        try {
            opencv_imgproc.erode(src, dst, kernel, anchor, iterations, borderType.value, borderValue);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
