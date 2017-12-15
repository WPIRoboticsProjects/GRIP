package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.Description;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGRA2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * An {@link Operation} that converts a color image into shades of gray.
 */
@Description(name = "Desaturate",
             summary = "Convert a color image into shades of gray",
             category = OperationDescription.Category.IMAGE_PROCESSING,
             iconName = "desaturate")
public class DesaturateOperation implements Operation {

  private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
  private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");

  private final InputSocket<Mat> inputSocket;
  private final OutputSocket<Mat> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public DesaturateOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.inputSocket = inputSocketFactory.create(inputHint);
    this.outputSocket = outputSocketFactory.create(outputHint);
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
        outputSocket
    );
  }

  @Override
  public void perform() {
    final Mat input = inputSocket.getValue().get();

    Mat output = outputSocket.getValue().get();


    switch (input.channels()) {
      case 1:
        // If the input is already one channel, it's already desaturated
        input.copyTo(output);
        break;

      case 3:
        cvtColor(input, output, COLOR_BGR2GRAY);
        break;

      case 4:
        cvtColor(input, output, COLOR_BGRA2GRAY);
        break;

      default:
        throw new IllegalArgumentException("Input to desaturate must have 1, 3, or 4 channels");
    }

    outputSocket.setValue(output);
  }
}
