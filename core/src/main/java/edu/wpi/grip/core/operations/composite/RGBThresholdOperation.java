package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import static org.bytedeco.opencv.global.opencv_core.inRange;

/**
 * An {@link Operation} that converts a color image into a binary image based on threshold ranges
 * for each channel.
 */
@Description(name = "RGB Threshold",
             summary = "Segment an image based on color ranges",
             category = OperationCategory.IMAGE_PROCESSING,
             iconName = "threshold")
public class RGBThresholdOperation extends ThresholdOperation {

  private static final Logger logger = Logger.getLogger(RGBThresholdOperation.class.getName());
  private final SocketHint<MatWrapper> inputHint = SocketHints.createImageSocketHint("Input");
  private final SocketHint<List<Number>> redHint = SocketHints.Inputs
      .createNumberListRangeSocketHint("Red", 0.0, 255.0);
  private final SocketHint<List<Number>> greenHint = SocketHints.Inputs
      .createNumberListRangeSocketHint("Green", 0.0, 255.0);
  private final SocketHint<List<Number>> blueHint = SocketHints.Inputs
      .createNumberListRangeSocketHint("Blue", 0.0, 255.0);

  private final SocketHint<MatWrapper> outputHint = SocketHints.createImageSocketHint("Output");


  private final InputSocket<MatWrapper> inputSocket;
  private final InputSocket<List<Number>> redSocket;
  private final InputSocket<List<Number>> greenSocket;
  private final InputSocket<List<Number>> blueSocket;

  private final OutputSocket<MatWrapper> outputSocket;

  @Inject
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
    final Mat input = inputSocket.getValue().get().getCpu();

    if (input.channels() != 3) {
      throw new IllegalArgumentException("RGB Threshold needs a 3-channel input");
    }

    final Mat output = outputSocket.getValue().get().rawCpu();
    final List<Number> channel1 = redSocket.getValue().get();
    final List<Number> channel2 = greenSocket.getValue().get();
    final List<Number> channel3 = blueSocket.getValue().get();

    final Scalar lowScalar = new Scalar(
        channel3.get(0).doubleValue(),
        channel2.get(0).doubleValue(),
        channel1.get(0).doubleValue(), 0);

    final Scalar highScalar = new Scalar(
        channel3.get(1).doubleValue(),
        channel2.get(1).doubleValue(),
        channel1.get(1).doubleValue(), 0);

    final Mat low = reallocateMatIfInputSizeOrWidthChanged(dataArray, 0, lowScalar, input);
    final Mat high = reallocateMatIfInputSizeOrWidthChanged(dataArray, 1, highScalar, input);

    try {
      inRange(input, low, high, output);

      outputSocket.flagChanged();
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }
}
