package edu.wpi.grip.core.operations.opencv;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;

public class MatFieldAccessor implements CVOperation {
    private static final Mat defaultsMat = new Mat();
    private final SocketHint matHint = new SocketHint("input", Mat.class, Mat::new);
    private final SocketHint sizeHint = new SocketHint("size", Size.class, Size::new);
    private final SocketHint emptyHint = new SocketHint("empty", Boolean.class, defaultsMat.empty());
    private final SocketHint channelsHint = new SocketHint("channels", Number.class, defaultsMat.channels());
    private final SocketHint colsHint = new SocketHint("cols", Number.class, defaultsMat.cols(), SocketHint.View.NONE, null, true);
    private final SocketHint rowsHint = new SocketHint("rows", Number.class, defaultsMat.rows(), SocketHint.View.NONE, null, true);
    private final SocketHint highValueHint = new SocketHint("high value", Number.class, defaultsMat.highValue());


    @Override
    public String getName() {
        return "Get Mat Info";
    }

    @Override
    public String getDescription() {
        return "Provide access to the various elements and properties of an OpenCV Mat object (an image).";
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket[]{new InputSocket(eventBus, matHint)};
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[]{
                new OutputSocket(eventBus, sizeHint),
                new OutputSocket(eventBus, emptyHint),
                new OutputSocket(eventBus, channelsHint),
                new OutputSocket(eventBus, colsHint),
                new OutputSocket(eventBus, rowsHint),
                new OutputSocket(eventBus, highValueHint)
        };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat inputMat = (Mat) inputs[0].getValue();
        final OutputSocket<Size> sizeSocket = (OutputSocket<Size>) outputs[0];
        final OutputSocket<Boolean> isEmptySocket = (OutputSocket<Boolean>) outputs[1];
        final OutputSocket<Number> channelsSocket = (OutputSocket<Number>) outputs[2];
        final OutputSocket<Number> colsSocket = (OutputSocket<Number>) outputs[3];
        final OutputSocket<Number> rowsSocket = (OutputSocket<Number>) outputs[4];
        final OutputSocket<Number> highValueSocket = (OutputSocket<Number>) outputs[5];

        sizeSocket.setValue(inputMat.size());
        isEmptySocket.setValue(inputMat.empty());
        channelsSocket.setValue(inputMat.channels());
        colsSocket.setValue(inputMat.cols());
        rowsSocket.setValue(inputMat.rows());
        highValueSocket.setValue(inputMat.highValue());
    }
}
