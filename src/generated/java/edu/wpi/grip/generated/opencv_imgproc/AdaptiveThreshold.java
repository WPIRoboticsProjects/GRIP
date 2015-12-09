package edu.wpi.grip.generated.opencv_imgproc;

import edu.wpi.grip.generated.opencv_imgproc.enumeration.ThresholdTypesEnum;
import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_imgproc;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.AdaptiveThresholdTypesEnum;
import edu.wpi.grip.core.operations.opencv.CVOperation;
import static org.bytedeco.javacpp.opencv_core.*;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * ===== THIS CODE HAS BEEN DYNAMICALLY GENERATED! DO NOT MODIFY! ==== *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
/** Operation to call {@link opencv_imgproc#adaptiveThreshold} */
public class AdaptiveThreshold implements CVOperation {

    private final SocketHint<Mat> srcInputHint = new SocketHint.Builder(Mat.class).identifier("src").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<Number> maxValueInputHint = new SocketHint.Builder(Number.class).identifier("maxValue").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build(), CInputHint = new SocketHint.Builder(Number.class).identifier("C").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build();

    private final SocketHint<AdaptiveThresholdTypesEnum> adaptiveMethodInputHint = new SocketHint.Builder(AdaptiveThresholdTypesEnum.class).identifier("adaptiveMethod").initialValue(AdaptiveThresholdTypesEnum.ADAPTIVE_THRESH_MEAN_C).view(SocketHint.View.SELECT).domain(AdaptiveThresholdTypesEnum.values()).build();

    private final SocketHint<Number> blockSizeInputHint = new SocketHint.Builder(Number.class).identifier("blockSize").initialValue(0).view(SocketHint.View.SPINNER).domain(new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE }).build();

    private final SocketHint<ThresholdTypesEnum> thresholdTypeInputHint = new SocketHint.Builder(ThresholdTypesEnum.class).identifier("thresholdType").initialValue(ThresholdTypesEnum.THRESH_BINARY).view(SocketHint.View.SELECT).domain(ThresholdTypesEnum.values()).build();

    private final SocketHint<Mat> dstOutputHint = new SocketHint.Builder(Mat.class).identifier("dst").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "adaptiveThreshold";
    }

    @Override
    public String getDescription() {
        return "Transforms a grayscale image to a binary image).";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, srcInputHint), new InputSocket(eventBus, maxValueInputHint), new InputSocket(eventBus, adaptiveMethodInputHint), new InputSocket(eventBus, thresholdTypeInputHint), new InputSocket(eventBus, blockSizeInputHint), new InputSocket(eventBus, CInputHint) };
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
        final double maxValue = ((Number) inputs[1].getValue().get()).doubleValue();
        final AdaptiveThresholdTypesEnum adaptiveMethod = (AdaptiveThresholdTypesEnum) inputs[2].getValue().get();
        final ThresholdTypesEnum thresholdType = (ThresholdTypesEnum) inputs[3].getValue().get();
        final int blockSize = ((Number) inputs[4].getValue().get()).intValue();
        final double C = ((Number) inputs[5].getValue().get()).doubleValue();
        try {
            opencv_imgproc.adaptiveThreshold(src, dst, maxValue, adaptiveMethod.value, thresholdType.value, blockSize, C);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
