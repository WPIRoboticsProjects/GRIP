package edu.wpi.grip.generated.opencv_imgproc;

import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_imgproc;
import edu.wpi.grip.core.operations.opencv.CVOperation;
import static org.bytedeco.javacpp.opencv_core.*;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.ColorConversionCodesEnum;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * ===== THIS CODE HAS BEEN DYNAMICALLY GENERATED! DO NOT MODIFY! ==== *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
/** Operation to call {@link opencv_imgproc#cvtColor} */
public class CvtColor implements CVOperation {

    private final SocketHint<Mat> srcInputHint = new SocketHint.Builder(Mat.class).identifier("src").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<ColorConversionCodesEnum> codeInputHint = new SocketHint.Builder(ColorConversionCodesEnum.class).identifier("code").initialValue(ColorConversionCodesEnum.COLOR_BGR2BGRA).view(SocketHint.View.SELECT).domain(ColorConversionCodesEnum.values()).build();

    private final SocketHint<Number> dstCnInputHint = new SocketHint.Builder(Number.class).identifier("dstCn").initialValue(0).view(SocketHint.View.SPINNER).domain(new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE }).build();

    private final SocketHint<Mat> dstOutputHint = new SocketHint.Builder(Mat.class).identifier("dst").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "cvtColor";
    }

    @Override
    public String getDescription() {
        return "Convert an image from one color space to another.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, srcInputHint), new InputSocket(eventBus, codeInputHint), new InputSocket(eventBus, dstCnInputHint) };
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
        final ColorConversionCodesEnum code = (ColorConversionCodesEnum) inputs[1].getValue().get();
        final int dstCn = ((Number) inputs[2].getValue().get()).intValue();
        try {
            opencv_imgproc.cvtColor(src, dst, code.value, dstCn);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
