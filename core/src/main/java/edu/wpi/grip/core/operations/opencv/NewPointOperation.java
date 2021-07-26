package edu.wpi.grip.core.operations.opencv;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.bytedeco.opencv.opencv_core.Point;

import java.util.List;

@Description(name = "New Point",
             summary = "Create a new point by (x,y) value",
             category = OperationCategory.OPENCV,
             iconName = "point")
public class NewPointOperation implements CVOperation {

  private final SocketHint<Number> xHint = SocketHints.Inputs.createNumberSpinnerSocketHint("x", -1,
      Integer.MIN_VALUE, Integer.MAX_VALUE);
  private final SocketHint<Number> yHint = SocketHints.Inputs.createNumberSpinnerSocketHint("y", -1,
      Integer.MIN_VALUE, Integer.MAX_VALUE);
  private final SocketHint<Point> outputHint = SocketHints.Outputs.createPointSocketHint("point");


  private final InputSocket<Number> xSocket;
  private final InputSocket<Number> ySocket;

  private final OutputSocket<Point> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public NewPointOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.xSocket = inputSocketFactory.create(xHint);
    this.ySocket = inputSocketFactory.create(yHint);

    this.outputSocket = outputSocketFactory.create(outputHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        xSocket,
        ySocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        outputSocket
    );
  }

  @Override
  public void perform() {
    final int xValue = xSocket.getValue().get().intValue();
    final int yValue = ySocket.getValue().get().intValue();
    outputSocket.setValue(new Point(xValue, yValue));
  }
}
