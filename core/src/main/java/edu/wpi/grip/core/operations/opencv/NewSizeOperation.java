package edu.wpi.grip.core.operations.opencv;


import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.inject.Inject;

import org.bytedeco.opencv.opencv_core.Size;
import org.python.google.common.collect.ImmutableList;

import java.util.List;

@Description(name = "New Size",
             summary = "Create a size by width and height values",
             category = OperationCategory.OPENCV,
             iconName = "size")
public class NewSizeOperation implements CVOperation {

  private final SocketHint<Number> widthHint = SocketHints.Inputs
      .createNumberSpinnerSocketHint("width", -1, -1, Integer.MAX_VALUE);
  private final SocketHint<Number> heightHint = SocketHints.Inputs
      .createNumberSpinnerSocketHint("height", -1, -1, Integer.MAX_VALUE);
  private final SocketHint<Size> outputHint = SocketHints.Outputs.createSizeSocketHint("size");


  private final InputSocket<Number> widthSocket;
  private final InputSocket<Number> heightSocket;

  private final OutputSocket<Size> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public NewSizeOperation(InputSocket.Factory inputSocketFactory,
                          OutputSocket.Factory outputSocketFactory) {
    this.widthSocket = inputSocketFactory.create(widthHint);
    this.heightSocket = inputSocketFactory.create(heightHint);

    this.outputSocket = outputSocketFactory.create(outputHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        widthSocket,
        heightSocket
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
    final int widthValue = widthSocket.getValue().get().intValue();
    final int heightValue = heightSocket.getValue().get().intValue();
    outputSocket.setValue(new Size(widthValue, heightValue));
  }
}

