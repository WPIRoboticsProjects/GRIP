package edu.wpi.grip.core.operations.opencv;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.List;

@Description(name = "Get Mat Info",
    summary = "Provide access to the various elements and properties of an image",
    category = OperationCategory.OPENCV,
    iconName = "opencv")
public class MatFieldAccessor implements CVOperation {

  private static final Mat defaultsMat = new Mat();
  private final SocketHint<MatWrapper> matHint = SocketHints.createImageSocketHint("Input");
  private final SocketHint<Size> sizeHint = SocketHints.Inputs.createSizeSocketHint("size", true);
  private final SocketHint<Boolean> emptyHint = SocketHints.Outputs
      .createBooleanSocketHint("empty", defaultsMat.empty());
  private final SocketHint<Number> channelsHint = SocketHints.Outputs
      .createNumberSocketHint("channels", defaultsMat.channels());
  private final SocketHint<Number> colsHint = SocketHints.Outputs
      .createNumberSocketHint("cols", defaultsMat.rows());
  private final SocketHint<Number> rowsHint = SocketHints.Outputs
      .createNumberSocketHint("rows", defaultsMat.rows());
  private final SocketHint<Number> highValueHint = SocketHints.Outputs
      .createNumberSocketHint("high value", defaultsMat.highValue());


  private final InputSocket<MatWrapper> inputSocket;

  private final OutputSocket<Size> sizeSocket;
  private final OutputSocket<Boolean> emptySocket;
  private final OutputSocket<Number> channelsSocket;
  private final OutputSocket<Number> colsSocket;
  private final OutputSocket<Number> rowsSocket;
  private final OutputSocket<Number> highValueSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public MatFieldAccessor(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
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
    MatWrapper wrapper = inputSocket.getValue().get();

    sizeSocket.setValue(wrapper.size());
    emptySocket.setValue(wrapper.empty());
    channelsSocket.setValue(wrapper.channels());
    colsSocket.setValue(wrapper.cols());
    rowsSocket.setValue(wrapper.rows());
    highValueSocket.setValue(wrapper.highValue());
  }
}
