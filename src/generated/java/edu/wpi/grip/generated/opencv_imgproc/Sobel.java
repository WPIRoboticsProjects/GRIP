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
/** Operation to call {@link opencv_imgproc#Sobel} */
public class Sobel implements CVOperation {

    private final SocketHint<Mat> srcInputHint = new SocketHint.Builder(Mat.class).identifier("src").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<Number> scaleInputHint = new SocketHint.Builder(Number.class).identifier("scale").initialValue(1.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build(), deltaInputHint = new SocketHint.Builder(Number.class).identifier("delta").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build();

    private final SocketHint<BorderTypesEnum> borderTypeInputHint = new SocketHint.Builder(BorderTypesEnum.class).identifier("borderType").initialValue(BorderTypesEnum.BORDER_DEFAULT).view(SocketHint.View.SELECT).domain(BorderTypesEnum.values()).build();

    private final SocketHint<Number> dxInputHint = new SocketHint.Builder(Number.class).identifier("dx").initialValue(0).view(SocketHint.View.SPINNER).domain(new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE }).build(), dyInputHint = new SocketHint.Builder(Number.class).identifier("dy").initialValue(0).view(SocketHint.View.SPINNER).domain(new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE }).build(), ksizeInputHint = new SocketHint.Builder(Number.class).identifier("ksize").initialValue(3).view(SocketHint.View.SPINNER).domain(new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE }).build();

    private final SocketHint<Mat> dstOutputHint = new SocketHint.Builder(Mat.class).identifier("dst").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "Sobel";
    }

    @Override
    public String getDescription() {
        return "Find edges by calculating the requested derivative order for the given image.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, srcInputHint), new InputSocket(eventBus, dxInputHint), new InputSocket(eventBus, dyInputHint), new InputSocket(eventBus, ksizeInputHint), new InputSocket(eventBus, scaleInputHint), new InputSocket(eventBus, deltaInputHint), new InputSocket(eventBus, borderTypeInputHint) };
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
        final int dx = ((Number) inputs[1].getValue().get()).intValue();
        final int dy = ((Number) inputs[2].getValue().get()).intValue();
        final int ksize = ((Number) inputs[3].getValue().get()).intValue();
        final double scale = ((Number) inputs[4].getValue().get()).doubleValue();
        final double delta = ((Number) inputs[5].getValue().get()).doubleValue();
        final BorderTypesEnum borderType = (BorderTypesEnum) inputs[6].getValue().get();
        try {
            opencv_imgproc.Sobel(src, dst, 0, dx, dy, ksize, scale, delta, borderType.value);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
