package edu.wpi.grip.generated.opencv_imgproc;

import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_imgproc;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.ColormapTypesEnum;
import edu.wpi.grip.core.operations.opencv.CVOperation;
import static org.bytedeco.javacpp.opencv_core.*;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * ===== THIS CODE HAS BEEN DYNAMICALLY GENERATED! DO NOT MODIFY! ==== *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
/** Operation to call {@link opencv_imgproc#applyColorMap} */
public class ApplyColorMap implements CVOperation {

    private final SocketHint<Mat> srcInputHint = new SocketHint.Builder(Mat.class).identifier("src").initialValue(null).view(SocketHint.View.NONE).build();

    private final SocketHint<ColormapTypesEnum> colormapInputHint = new SocketHint.Builder(ColormapTypesEnum.class).identifier("colormap").initialValue(ColormapTypesEnum.COLORMAP_AUTUMN).view(SocketHint.View.SELECT).domain(ColormapTypesEnum.values()).build();

    private final SocketHint<Mat> dstOutputHint = new SocketHint.Builder(Mat.class).identifier("dst").initialValueSupplier(Mat::new).view(SocketHint.View.NONE).build();

    @Override
    public String getName() {
        return "applyColorMap";
    }

    @Override
    public String getDescription() {
        return "Apply a MATLAB equivalent colormap to an image.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[] { new InputSocket(eventBus, srcInputHint), new InputSocket(eventBus, colormapInputHint) };
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
        final ColormapTypesEnum colormap = (ColormapTypesEnum) inputs[1].getValue().get();
        try {
            opencv_imgproc.applyColorMap(src, dst, colormap.value);
            for (OutputSocket outputSocket : outputs) {
                outputSocket.setValueOptional(outputSocket.getValue());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}