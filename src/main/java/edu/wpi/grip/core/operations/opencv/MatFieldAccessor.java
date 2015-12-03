package edu.wpi.grip.core.operations.opencv;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.SocketHints;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;

public class MatFieldAccessor implements CVOperation {
    private static final Mat defaultsMat = new Mat();
    private final SocketHint matHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint sizeHint = SocketHints.Inputs.createSizeSocketHint("size", true);
    private final SocketHint emptyHint = SocketHints.Outputs.createBooleanSocketHint("empty", defaultsMat.empty());
    private final SocketHint channelsHint = SocketHints.Outputs.createNumberSocketHint("channels", defaultsMat.channels());
    private final SocketHint colsHint = SocketHints.Outputs.createNumberSocketHint("cols", defaultsMat.rows());
    private final SocketHint rowsHint = SocketHints.Outputs.createNumberSocketHint("rows", defaultsMat.rows());
    private final SocketHint highValueHint = SocketHints.Outputs.createNumberSocketHint("high value", defaultsMat.highValue());


    @Override
    public String getName() {
        return "Get Mat Info";
    }

    @Override
    public String getDescription() {
        return "Provide access to the various elements and properties of an image.";
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
        final Mat inputMat = (Mat) inputs[0].getValue().get();
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
