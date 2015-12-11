package edu.wpi.grip.generated.opencv_core;

import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_core;
import edu.wpi.grip.core.operations.opencv.CVOperation;
import static org.bytedeco.javacpp.opencv_core.*;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * ===== THIS CODE HAS BEEN DYNAMICALLY GENERATED! DO NOT MODIFY! ==== *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
/** Operation to call {@link opencv_core#addWeighted} */
public class AddWeighted implements CVOperation {

    private final SocketHint<Mat> src1InputHint = new SocketHint.Builder(Mat.class).identifier("src1").initialValue(null).view(SocketHint.View.NONE).build(), src2InputHint = new SocketHint.Builder(Mat.class).identifier("src2").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<Number> alphaInputHint = new SocketHint.Builder(Number.class).identifier("alpha").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build(), betaInputHint = new SocketHint.Builder(Number.class).identifier("beta").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build(), gammaInputHint = new SocketHint.Builder(Number.class).identifier("gamma").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build();

    private final SocketHint<Mat> dstOutputHint = new SocketHint.Builder(Mat.class).identifier("dst").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "addWeighted";
    }

    @Override
    public String getDescription() {
        return "Calculate the weighted sum of two images.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, src1InputHint), new InputSocket(eventBus, alphaInputHint), new InputSocket(eventBus, src2InputHint), new InputSocket(eventBus, betaInputHint), new InputSocket(eventBus, gammaInputHint) };
    }

    @Override
    @SuppressWarnings("unchecked")
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[] { new OutputSocket(eventBus, dstOutputHint) };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat src1 = (Mat) inputs[0].getValue().get();
        final double alpha = ((Number) inputs[1].getValue().get()).doubleValue();
        final Mat src2 = (Mat) inputs[2].getValue().get();
        final double beta = ((Number) inputs[3].getValue().get()).doubleValue();
        final double gamma = ((Number) inputs[4].getValue().get()).doubleValue();
        final Mat dst = (Mat) outputs[0].getValue().get();
        try {
            opencv_core.addWeighted(src1, alpha, src2, beta, gamma, dst, -1);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
