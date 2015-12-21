package edu.wpi.grip.generated.opencv_imgproc;

import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_imgproc;
import edu.wpi.grip.core.operations.opencv.CVOperation;
import static org.bytedeco.javacpp.opencv_core.*;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.generated.opencv_core.enumeration.LineTypesEnum;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * ===== THIS CODE HAS BEEN DYNAMICALLY GENERATED! DO NOT MODIFY! ==== *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
/** Operation to call {@link opencv_imgproc#rectangle} */
public class Rectangle implements CVOperation {

    private final SocketHint<Mat> imgInputHint = new SocketHint.Builder(Mat.class).identifier("img").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    private final SocketHint<LineTypesEnum> lineTypeInputHint = new SocketHint.Builder(LineTypesEnum.class).identifier("lineType").initialValue(LineTypesEnum.LINE_8).view(SocketHint.View.SELECT).domain(LineTypesEnum.values()).build();

    private final SocketHint<Point> pt1InputHint = new SocketHint.Builder(Point.class).identifier("pt1").initialValue(null).view(SocketHint.View.NONE).build(), pt2InputHint = new SocketHint.Builder(Point.class).identifier("pt2").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<Number> thicknessInputHint = new SocketHint.Builder(Number.class).identifier("thickness").initialValue(1).view(SocketHint.View.SPINNER).domain(new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE }).build(), shiftInputHint = new SocketHint.Builder(Number.class).identifier("shift").initialValue(0).view(SocketHint.View.SPINNER).domain(new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE }).build();

    private final SocketHint<Scalar> colorInputHint = new SocketHint.Builder(Scalar.class).identifier("color").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<Mat> imgOutputHint = new SocketHint.Builder(Mat.class).identifier("img").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "rectangle";
    }

    @Override
    public String getDescription() {
        return "Draw a rectangle (outline or filled) on an image.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, imgInputHint), new InputSocket(eventBus, pt1InputHint), new InputSocket(eventBus, pt2InputHint), new InputSocket(eventBus, colorInputHint), new InputSocket(eventBus, thicknessInputHint), new InputSocket(eventBus, lineTypeInputHint), new InputSocket(eventBus, shiftInputHint) };
    }

    @Override
    @SuppressWarnings("unchecked")
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[] { new OutputSocket(eventBus, imgOutputHint) };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        /* Sets the value of the input Mat to the output because this operation does not have a destination Mat. */
        ((InputSocket<Mat>) inputs[0]).getValue().get().assignTo(((OutputSocket<Mat>) outputs[0]).getValue().get());
        final Mat img = (Mat) outputs[0].getValue().get();
        final Point pt1 = (Point) inputs[1].getValue().get();
        final Point pt2 = (Point) inputs[2].getValue().get();
        final Scalar color = (Scalar) inputs[3].getValue().get();
        final int thickness = ((Number) inputs[4].getValue().get()).intValue();
        final LineTypesEnum lineType = (LineTypesEnum) inputs[5].getValue().get();
        final int shift = ((Number) inputs[6].getValue().get()).intValue();
        try {
            opencv_imgproc.rectangle(img, pt1, pt2, color, thickness, lineType.value, shift);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
