package edu.wpi.grip.core.operations.opencv;

import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;

import java.util.List;

public class MatFieldAccessor implements CVOperation {

    public static final OperationDescription DESCRIPTION =
             CVOperation.defaultBuilder()
                    .name("Get Mat Info")
                    .summary("Provide access to the various elements and properties of an image.")
                    .build();

    private static final Mat defaultsMat = new Mat();
    private final SocketHint<Mat> matHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<Size> sizeHint = SocketHints.Inputs.createSizeSocketHint("size", true);
    private final SocketHint<Boolean> emptyHint = SocketHints.Outputs.createBooleanSocketHint("empty", defaultsMat.empty());
    private final SocketHint<Number> channelsHint = SocketHints.Outputs.createNumberSocketHint("channels", defaultsMat.channels());
    private final SocketHint<Number> colsHint = SocketHints.Outputs.createNumberSocketHint("cols", defaultsMat.rows());
    private final SocketHint<Number> rowsHint = SocketHints.Outputs.createNumberSocketHint("rows", defaultsMat.rows());
    private final SocketHint<Number> highValueHint = SocketHints.Outputs.createNumberSocketHint("high value", defaultsMat.highValue());


    private final InputSocket<Mat> inputSocket;

    private final OutputSocket<Size> sizeSocket;
    private final OutputSocket<Boolean> emptySocket;
    private final OutputSocket<Number> channelsSocket;
    private final OutputSocket<Number> colsSocket;
    private final OutputSocket<Number> rowsSocket;
    private final OutputSocket<Number> highValueSocket;

    public MatFieldAccessor(InputSocket.Factory inputSocketFactory, OutputSocket.Factory outputSocketFactory) {
        this.inputSocket = inputSocketFactory.create(matHint);

        this.sizeSocket = outputSocketFactory.create(sizeHint);
        this.emptySocket = outputSocketFactory.create(emptyHint);
        this.channelsSocket = outputSocketFactory.create(channelsHint);
        this.colsSocket = outputSocketFactory.create(colsHint);
        this.rowsSocket = outputSocketFactory.create(rowsHint);
        this.highValueSocket = outputSocketFactory.create(highValueHint);
    }

    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.of(
                inputSocket
        );
    }

    @Override
    public List<OutputSocket> getOutputSockets() {
        return ImmutableList.of(
                sizeSocket,
                emptySocket,
                channelsSocket,
                colsSocket,
                rowsSocket,
                highValueSocket
        );
    }

    @Override
    public void perform() {
        final Mat inputMat = inputSocket.getValue().get();

        sizeSocket.setValue(inputMat.size());
        emptySocket.setValue(inputMat.empty());
        channelsSocket.setValue(inputMat.channels());
        colsSocket.setValue(inputMat.cols());
        rowsSocket.setValue(inputMat.rows());
        highValueSocket.setValue(inputMat.highValue());
    }
}
