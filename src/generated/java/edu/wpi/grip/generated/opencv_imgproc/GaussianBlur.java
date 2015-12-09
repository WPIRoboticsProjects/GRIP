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
/** Operation to call {@link opencv_imgproc#GaussianBlur} */
public class GaussianBlur implements CVOperation {

    private final SocketHint<Mat> srcInputHint = new SocketHint.Builder(Mat.class).identifier("src").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<Size> ksizeInputHint = new SocketHint.Builder(Size.class).identifier("ksize").initialValueSupplier(() -> new Size(1, 1)).view(SocketHint.View.NONE).build();

    private final SocketHint<Number> sigmaXInputHint = new SocketHint.Builder(Number.class).identifier("sigmaX").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build(), sigmaYInputHint = new SocketHint.Builder(Number.class).identifier("sigmaY").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build();

    private final SocketHint<BorderTypesEnum> borderTypeInputHint = new SocketHint.Builder(BorderTypesEnum.class).identifier("borderType").initialValue(BorderTypesEnum.BORDER_DEFAULT).view(SocketHint.View.SELECT).domain(BorderTypesEnum.values()).build();

    private final SocketHint<Mat> dstOutputHint = new SocketHint.Builder(Mat.class).identifier("dst").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "GaussianBlur";
    }

    @Override
    public String getDescription() {
        return "Apply a Gaussian blur to an image.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, srcInputHint), new InputSocket(eventBus, ksizeInputHint), new InputSocket(eventBus, sigmaXInputHint), new InputSocket(eventBus, sigmaYInputHint), new InputSocket(eventBus, borderTypeInputHint) };
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
        final Size ksize = (Size) inputs[1].getValue().get();
        final double sigmaX = ((Number) inputs[2].getValue().get()).doubleValue();
        final double sigmaY = ((Number) inputs[3].getValue().get()).doubleValue();
        final BorderTypesEnum borderType = (BorderTypesEnum) inputs[4].getValue().get();
        try {
            opencv_imgproc.GaussianBlur(src, dst, ksize, sigmaX, sigmaY, borderType.value);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
