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
/** Operation to call {@link opencv_core#subtract} */
public class Subtract implements CVOperation {

    private final SocketHint<Mat> src1InputHint = new SocketHint.Builder(Mat.class).identifier("src1").initialValue(null).view(SocketHint.View.NONE).build(), src2InputHint = new SocketHint.Builder(Mat.class).identifier("src2").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<Mat> dstOutputHint = new SocketHint.Builder(Mat.class).identifier("dst").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "subtract";
    }

    @Override
    public String getDescription() {
        return "Calculate the per-pixel difference between two images.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, src1InputHint), new InputSocket(eventBus, src2InputHint) };
    }

    @Override
    @SuppressWarnings("unchecked")
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[] { new OutputSocket(eventBus, dstOutputHint) };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat src1 = (Mat) inputs[0].getValue().get();
        final Mat src2 = (Mat) inputs[1].getValue().get();
        final Mat dst = (Mat) outputs[0].getValue().get();
        try {
            opencv_core.subtract(src1, src2, dst);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
