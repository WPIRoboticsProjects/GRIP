package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.bitwise_xor;

/**
 * An {@link Operation} that masks out an area of interest from an image.
 */
@Description(name = "Mask",
             summary = "Filter out an area of interest in an image using a binary mask",
             category = OperationCategory.IMAGE_PROCESSING,
             iconName = "mask")
public class MaskOperation implements Operation {

  private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
  private final SocketHint<Mat> maskHint = SocketHints.Inputs.createMatSocketHint("Mask", false);

  private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");


  private final InputSocket<Mat> inputSocket;
  private final InputSocket<Mat> maskSocket;

  private final OutputSocket<Mat> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public MaskOperation(InputSocket.Factory inputSocketFactory,
                       OutputSocket.Factory outputSocketFactory) {
    this.inputSocket = inputSocketFactory.create(inputHint);
    this.maskSocket = inputSocketFactory.create(maskHint);

    this.outputSocket = outputSocketFactory.create(outputHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        maskSocket
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
    final Mat mask = maskSocket.getValue().get();

    final Mat output = outputSocket.getValue().get();

    // Clear the output to black, then copy the input to it with the mask
    bitwise_xor(output, output, output);
    input.copyTo(output, mask);
    outputSocket.setValue(output);
  }
}
