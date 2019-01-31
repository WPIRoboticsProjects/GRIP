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
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_imgproc.GaussianBlur;
import static org.bytedeco.javacpp.opencv_imgproc.bilateralFilter;
import static org.bytedeco.javacpp.opencv_imgproc.blur;
import static org.bytedeco.javacpp.opencv_imgproc.medianBlur;

/**
 * An {@link Operation} that softens an image using one of several different filters.
 */
@Description(name = "Blur",
             summary = "Blurs an image to remove noise",
             category = OperationCategory.IMAGE_PROCESSING,
             iconName = "blur")
public class BlurOperation implements Operation {

  private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
  private final SocketHint<Type> typeHint = SocketHints.createEnumSocketHint("Type", Type.BOX);
  private final SocketHint<Number> radiusHint = SocketHints.Inputs
      .createNumberSliderSocketHint("Radius", 0.0, 0.0, 100.0);
  private final SocketHint<Mat> outputHint = SocketHints.Inputs.createMatSocketHint("Output", true);
  private final InputSocket<Mat> inputSocket;
  private final InputSocket<Type> typeSocket;
  private final InputSocket<Number> radiusSocket;
  private final OutputSocket<Mat> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public BlurOperation(InputSocket.Factory inputSocketFactory,
                       OutputSocket.Factory outputSocketFactory) {
    this.inputSocket = inputSocketFactory.create(inputHint);
    this.typeSocket = inputSocketFactory.create(typeHint);
    this.radiusSocket = inputSocketFactory.create(radiusHint);

    this.outputSocket = outputSocketFactory.create(outputHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        typeSocket,
        radiusSocket
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
    final Type type = typeSocket.getValue().get();
    final Number radius = radiusSocket.getValue().get();

    final Mat output = outputSocket.getValue().get();

    int kernelSize;

    switch (type) {
      case BOX:
        // Box filter kernels must have an odd size
        kernelSize = 2 * radius.intValue() + 1;
        blur(input, output, new Size(kernelSize, kernelSize));
        break;

      case GAUSSIAN:
        // A Gaussian blur radius is a standard deviation, so a kernel that extends three radii
        // in either direction
        // from the center should account for 99.7% of the theoretical influence on each pixel.
        kernelSize = 6 * radius.intValue() + 1;
        GaussianBlur(input, output, new Size(kernelSize, kernelSize), radius.doubleValue());
        break;

      case MEDIAN:
        kernelSize = 2 * radius.intValue() + 1;
        medianBlur(input, output, kernelSize);
        break;

      case BILATERAL_FILTER:
        bilateralFilter(input, output, -1, radius.doubleValue(), radius.doubleValue());
        break;

      default:
        throw new IllegalArgumentException("Illegal blur type: " + type);
    }

    outputSocket.setValue(output);
  }

  private enum Type {
    BOX("Box Blur"), GAUSSIAN("Gaussian Blur"), MEDIAN("Median Filter"),
    BILATERAL_FILTER("Bilateral Filter");

    private final String label;

    Type(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return this.label;
    }
  }
}
