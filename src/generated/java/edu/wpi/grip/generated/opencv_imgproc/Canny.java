package edu.wpi.grip.generated.opencv_imgproc;

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
/** Operation to call {@link opencv_imgproc#Canny} */
public class Canny implements CVOperation {

    private final SocketHint<Mat> imageInputHint = new SocketHint.Builder(Mat.class).identifier("image").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<Boolean> L2gradientInputHint = new SocketHint.Builder(Boolean.class).identifier("L2gradient").initialValue(false).view(SocketHint.View.CHECKBOX).domain(new Boolean[] { true, false }).build();

    private final SocketHint<Number> threshold1InputHint = new SocketHint.Builder(Number.class).identifier("threshold1").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build(), threshold2InputHint = new SocketHint.Builder(Number.class).identifier("threshold2").initialValue(0.0).view(SocketHint.View.SPINNER).domain(new Double[] { -Double.MAX_VALUE, Double.MAX_VALUE }).build();

    private final SocketHint<Number> apertureSizeInputHint = new SocketHint.Builder(Number.class).identifier("apertureSize").initialValue(3).view(SocketHint.View.SPINNER).domain(new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE }).build();

    private final SocketHint<Mat> edgesOutputHint = new SocketHint.Builder(Mat.class).identifier("edges").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "Canny";
    }

    @Override
    public String getDescription() {
        return "Apply a \"canny edge detection\" algorithm to an image.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, imageInputHint), new InputSocket(eventBus, threshold1InputHint), new InputSocket(eventBus, threshold2InputHint), new InputSocket(eventBus, apertureSizeInputHint), new InputSocket(eventBus, L2gradientInputHint) };
    }

    @Override
    @SuppressWarnings("unchecked")
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[] { new OutputSocket(eventBus, edgesOutputHint) };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat image = (Mat) inputs[0].getValue().get();
        final Mat edges = (Mat) outputs[0].getValue().get();
        final double threshold1 = ((Number) inputs[1].getValue().get()).doubleValue();
        final double threshold2 = ((Number) inputs[2].getValue().get()).doubleValue();
        final int apertureSize = ((Number) inputs[3].getValue().get()).intValue();
        final boolean L2gradient = (Boolean) inputs[4].getValue().get();
        try {
            opencv_imgproc.Canny(image, edges, threshold1, threshold2, apertureSize, L2gradient);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
