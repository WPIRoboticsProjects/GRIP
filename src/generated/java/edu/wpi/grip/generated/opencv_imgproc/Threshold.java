package edu.wpi.grip.generated.opencv_imgproc;

import edu.wpi.grip.generated.opencv_imgproc.enumeration.ThresholdTypesEnum;
import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_imgproc;
import edu.wpi.grip.core.operations.opencv.CVOperation;
import static org.bytedeco.javacpp.opencv_core.*;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * ===== THIS CODE HAS BEEN DYNAMICALLY GENERATED! DO NOT MODIFY! ==== *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
/** Operation to call {@link opencv_imgproc#threshold} */
public class Threshold implements CVOperation {

    private final SocketHint<Mat> srcInputHint = new SocketHint.Builder(Mat.class).identifier("src").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<Number> threshInputHint = new SocketHint.Builder(Number.class).identifier("thresh").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build(), maxvalInputHint = new SocketHint.Builder(Number.class).identifier("maxval").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build();

    private final SocketHint<ThresholdTypesEnum> typeInputHint = new SocketHint.Builder(ThresholdTypesEnum.class).identifier("type").initialValue(ThresholdTypesEnum.THRESH_BINARY).view(SocketHint.View.SELECT).domain(ThresholdTypesEnum.values()).build();

    private final SocketHint<Mat> dstOutputHint = new SocketHint.Builder(Mat.class).identifier("dst").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "threshold";
    }

    @Override
    public String getDescription() {
        return "Apply a fixed-level threshold to each array element in an image.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, srcInputHint), new InputSocket(eventBus, threshInputHint), new InputSocket(eventBus, maxvalInputHint), new InputSocket(eventBus, typeInputHint) };
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
        final double thresh = ((Number) inputs[1].getValue().get()).doubleValue();
        final double maxval = ((Number) inputs[2].getValue().get()).doubleValue();
        final ThresholdTypesEnum type = (ThresholdTypesEnum) inputs[3].getValue().get();
        try {
            opencv_imgproc.threshold(src, dst, thresh, maxval, type.value);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
