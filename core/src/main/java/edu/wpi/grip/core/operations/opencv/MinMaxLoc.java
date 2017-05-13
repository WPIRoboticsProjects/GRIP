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

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;

import java.util.List;

/**
 * Operation to call {@link opencv_core#minMaxLoc}.
 */
@Description(name = "Find Min and Max",
             summary = "Find the global minimum and maximum in a single channel grayscale image",
             category = OperationCategory.OPENCV,
             iconName = "opencv")
public class MinMaxLoc implements CVOperation {

  private final SocketHint<MatWrapper> srcInputHint = SocketHints.createImageSocketHint("Image");
  private final SocketHint<MatWrapper> maskInputHint = SocketHints.createImageSocketHint("Mask");

  private final SocketHint<Number> minValOutputHint = SocketHints.Outputs
      .createNumberSocketHint("Min Val", 0);
  private final SocketHint<Number> maxValOutputHint = SocketHints.Outputs
      .createNumberSocketHint("Max Val", 0);

  private final SocketHint<Point> minLocOutputHint = SocketHints.Outputs
      .createPointSocketHint("Min Loc");
  private final SocketHint<Point> maxLocOutputHint = SocketHints.Outputs
      .createPointSocketHint("Max Loc");

  private final InputSocket<MatWrapper> srcSocket;
  private final InputSocket<MatWrapper> maskSocket;

  private final OutputSocket<Number> minValSocket;
  private final OutputSocket<Number> maxValSocket;
  private final OutputSocket<Point> minLocSocket;
  private final OutputSocket<Point> maxLocSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public MinMaxLoc(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.srcSocket = inputSocketFactory.create(srcInputHint);
    this.maskSocket = inputSocketFactory.create(maskInputHint);

    this.minValSocket = outputSocketFactory.create(minValOutputHint);
    this.maxValSocket = outputSocketFactory.create(maxValOutputHint);
    this.minLocSocket = outputSocketFactory.create(minLocOutputHint);
    this.maxLocSocket = outputSocketFactory.create(maxLocOutputHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        srcSocket,
        maskSocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        minValSocket,
        maxValSocket,
        minLocSocket,
        maxLocSocket
    );
  }

  @Override
  public void perform() {
    final Mat src = srcSocket.getValue().get().getCpu();
    Mat mask = maskSocket.getValue().get().getCpu();
    if (mask.empty()) {
      mask = null;
    }
    DoublePointer minVal = new DoublePointer(0.0);
    DoublePointer maxVal = new DoublePointer(0.0);
    final Point minLoc = minLocSocket.getValue().get();
    final Point maxLoc = maxLocSocket.getValue().get();

    opencv_core.minMaxLoc(src, minVal, maxVal, minLoc, maxLoc, mask);
    minValSocket.setValue(minVal.get());
    maxValSocket.setValue(maxVal.get());
    minLocSocket.flagChanged();
    maxLocSocket.flagChanged();
  }
}
