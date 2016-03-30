package edu.wpi.grip.core.operations.opencv;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.*;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;

public class MatFieldAccessor implements CVOperation {
    private static final Mat defaultsMat = new Mat();
    private final SocketHint<Mat> matHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<Size> sizeHint = SocketHints.Inputs.createSizeSocketHint("size", true);
    private final SocketHint<Boolean> emptyHint = SocketHints.Outputs.createBooleanSocketHint("empty", defaultsMat.empty());
    private final SocketHint<Number> channelsHint = SocketHints.Outputs.createNumberSocketHint("channels", defaultsMat.channels());
    private final SocketHint<Number> colsHint = SocketHints.Outputs.createNumberSocketHint("cols", defaultsMat.rows());
    private final SocketHint<Number> rowsHint = SocketHints.Outputs.createNumberSocketHint("rows", defaultsMat.rows());
    private final SocketHint<Number> highValueHint = SocketHints.Outputs.createNumberSocketHint("high value", defaultsMat.highValue());


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
        return new InputSocket[]{new InputSocket<>(eventBus, matHint)};
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[]{
                new OutputSocket<>(eventBus, sizeHint),
                new OutputSocket<>(eventBus, emptyHint),
                new OutputSocket<>(eventBus, channelsHint),
                new OutputSocket<>(eventBus, colsHint),
                new OutputSocket<>(eventBus, rowsHint),
                new OutputSocket<>(eventBus, highValueHint)
        };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat inputMat = matHint.retrieveValue(inputs[0]);
        final Socket<Size> sizeSocket = sizeHint.safeCastSocket(outputs[0]);
        final Socket<Boolean> isEmptySocket = emptyHint.safeCastSocket(outputs[1]);
        final Socket<Number> channelsSocket = channelsHint.safeCastSocket(outputs[2]);
        final Socket<Number> colsSocket = colsHint.safeCastSocket(outputs[3]);
        final Socket<Number> rowsSocket = rowsHint.safeCastSocket(outputs[4]);
        final Socket<Number> highValueSocket = highValueHint.safeCastSocket(outputs[5]);

        sizeSocket.setValue(inputMat.size());
        isEmptySocket.setValue(inputMat.empty());
        channelsSocket.setValue(inputMat.channels());
        colsSocket.setValue(inputMat.cols());
        rowsSocket.setValue(inputMat.rows());
        highValueSocket.setValue(inputMat.highValue());
    }
}
