package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.Range;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Scalar;
import static org.bytedeco.javacpp.opencv_core.inRange;

/**
 * An {@link Operation} that converts a color image into a binary image based on threshold ranges
 * for each channel.
 */
public class RGBThresholdOperation extends ThresholdOperation {

  public static final OperationDescription DESCRIPTION =
      OperationDescription.builder()
          .name("RGB Threshold")
          .summary("Segment an image based on color ranges")
          .category(OperationDescription.Category.IMAGE_PROCESSING)
          .icon(Icon.iconStream("threshold"))
          .build();

  private static final Logger logger = Logger.getLogger(RGBThresholdOperation.class.getName());
  private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
  private final SocketHint<Range> redHint = SocketHints.Inputs
      .createNumberRangeSocketHint("Red", 0.0, 255.0);
  private final SocketHint<Range> greenHint = SocketHints.Inputs
      .createNumberRangeSocketHint("Green", 0.0, 255.0);
  private final SocketHint<Range> blueHint = SocketHints.Inputs
      .createNumberRangeSocketHint("Blue", 0.0, 255.0);

  private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");


  private final InputSocket<Mat> inputSocket;
  private final InputSocket<Range> redSocket;
  private final InputSocket<Range> greenSocket;
  private final InputSocket<Range> blueSocket;

  private final OutputSocket<Mat> outputSocket;

  @SuppressWarnings("JavadocMethod")
  public RGBThresholdOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.inputSocket = inputSocketFactory.create(inputHint);
    this.redSocket = inputSocketFactory.create(redHint);
    this.greenSocket = inputSocketFactory.create(greenHint);
    this.blueSocket = inputSocketFactory.create(blueHint);

    this.outputSocket = outputSocketFactory.create(outputHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        redSocket,
        greenSocket,
        blueSocket
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

    if (input.channels() != 3) {
      throw new IllegalArgumentException("RGB Threshold needs a 3-channel input");
    }

    final Mat output = outputSocket.getValue().get();
    final Range channel1 = redSocket.getValue().get();
    final Range channel2 = greenSocket.getValue().get();
    final Range channel3 = blueSocket.getValue().get();

    final Scalar lowScalar = new Scalar(
        channel3.getMin(),
        channel2.getMin(),
        channel1.getMin(), 0);

    final Scalar highScalar = new Scalar(
        channel3.getMax(),
        channel2.getMax(),
        channel1.getMax(), 0);

    final Mat low = reallocateMatIfInputSizeOrWidthChanged(dataArray, 0, lowScalar, input);
    final Mat high = reallocateMatIfInputSizeOrWidthChanged(dataArray, 1, highScalar, input);

    try {
      inRange(input, low, high, output);

      outputSocket.setValue(output);
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }
}
