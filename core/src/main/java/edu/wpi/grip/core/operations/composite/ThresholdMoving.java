package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;

import java.util.List;

/**
 * Finds the absolute difference between the current image and the previous image.
 */
@Description(name = "Threshold Moving",
             summary = "Thresholds off parts of the image that have moved or changed between the"
                 + " previous and next image.")
public class ThresholdMoving implements Operation {

  private final InputSocket<Mat> imageSocket;
  private final OutputSocket<Mat> outputSocket;
  private final Mat lastImage;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public ThresholdMoving(InputSocket.Factory inputSocketFactory,
                         OutputSocket.Factory outputSocketFactory) {
    imageSocket = inputSocketFactory.create(SocketHints.Inputs.createMatSocketHint("image", false));
    outputSocket = outputSocketFactory.create(SocketHints.Outputs.createMatSocketHint("moved"));
    lastImage = new Mat();
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        imageSocket
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
    final Mat input = imageSocket.getValue().get();
    final Size lastSize = lastImage.size();
    final Size inputSize = input.size();
    if (!lastImage.empty() && lastSize.height() == inputSize.height()
        && lastSize.width() == inputSize.width()) {
      opencv_core.absdiff(input, lastImage, outputSocket.getValue().get());
    }
    input.copyTo(lastImage);
    outputSocket.setValue(outputSocket.getValue().get());
  }
}
