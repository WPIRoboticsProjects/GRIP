package edu.wpi.grip.generated.opencv_imgproc;

import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_imgproc;
import edu.wpi.grip.core.operations.opencv.CVOperation;
import static org.bytedeco.javacpp.opencv_core.*;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.InterpolationFlagsEnum;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * ===== THIS CODE HAS BEEN DYNAMICALLY GENERATED! DO NOT MODIFY! ==== *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
/** Operation to call {@link opencv_imgproc#resize} */
public class Resize implements CVOperation {

    private final SocketHint<Mat> srcInputHint = new SocketHint.Builder(Mat.class).identifier("src").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<Size> dsizeInputHint = new SocketHint.Builder(Size.class).identifier("dsize").initialValueSupplier(Size::new).view(SocketHint.View.NONE).build();

    private final SocketHint<Number> fxInputHint = new SocketHint.Builder(Number.class).identifier("fx").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build(), fyInputHint = new SocketHint.Builder(Number.class).identifier("fy").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build();

    private final SocketHint<InterpolationFlagsEnum> interpolationInputHint = new SocketHint.Builder(InterpolationFlagsEnum.class).identifier("interpolation").initialValue(InterpolationFlagsEnum.INTER_LINEAR).view(SocketHint.View.SELECT).domain(InterpolationFlagsEnum.values()).build();

    private final SocketHint<Mat> dstOutputHint = new SocketHint.Builder(Mat.class).identifier("dst").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "resize";
    }

    @Override
    public String getDescription() {
        return "Resize the image to the specified size.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, srcInputHint), new InputSocket(eventBus, dsizeInputHint), new InputSocket(eventBus, fxInputHint), new InputSocket(eventBus, fyInputHint), new InputSocket(eventBus, interpolationInputHint) };
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
        final Size dsize = (Size) inputs[1].getValue().get();
        final double fx = ((Number) inputs[2].getValue().get()).doubleValue();
        final double fy = ((Number) inputs[3].getValue().get()).doubleValue();
        final InterpolationFlagsEnum interpolation = (InterpolationFlagsEnum) inputs[4].getValue().get();
        try {
            opencv_imgproc.resize(src, dst, dsize, fx, fy, interpolation.value);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
