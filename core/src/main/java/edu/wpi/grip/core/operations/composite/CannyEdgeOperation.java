package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.operations.CudaOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.bytedeco.javacpp.opencv_cudaimgproc.CannyEdgeDetector;

import java.util.List;

import static org.bytedeco.javacpp.opencv_cudaimgproc.createCannyEdgeDetector;
import static org.bytedeco.javacpp.opencv_imgproc.Canny;

/**
 * An operation that performs canny edge detection on an image.
 */
@Description(name = "CV Canny",
             summary = "Performs canny edge detection on a grayscale image",
             category = OperationCategory.OPENCV,
             iconName = "opencv")
public class CannyEdgeOperation extends CudaOperation {

  private final SocketHint<Number> lowThreshHint
      = SocketHints.Inputs.createNumberSpinnerSocketHint("Low threshold", 0);
  private final SocketHint<Number> highThreshHint
      = SocketHints.Inputs.createNumberSpinnerSocketHint("High threshold", 0);
  private final SocketHint<Number> apertureSizeHint
      = SocketHints.Inputs.createNumberSpinnerSocketHint("Aperture size", 0);
  private final SocketHint<Boolean> l2gradientHint
      = SocketHints.Inputs.createCheckboxSocketHint("L2gradient", false);

  private final InputSocket<Number> lowThreshSocket;
  private final InputSocket<Number> highThreshSocket;
  private final InputSocket<Number> apertureSizeSocket;
  private final InputSocket<Boolean> l2gradientSocket;

  @Inject
  protected CannyEdgeOperation(InputSocket.Factory isf, OutputSocket.Factory osf) {
    super(isf, osf);
    lowThreshSocket = isf.create(lowThreshHint);
    highThreshSocket = isf.create(highThreshHint);
    apertureSizeSocket = isf.create(apertureSizeHint);
    l2gradientSocket = isf.create(l2gradientHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        lowThreshSocket,
        highThreshSocket,
        apertureSizeSocket,
        l2gradientSocket,
        gpuSocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        outputSocket
    );
  }

  @Override
  @SuppressFBWarnings(value = "RCN", justification = "False positive (there is no nullcheck)")
  public void perform() {
    double lowThresh = lowThreshSocket.getValue().get().doubleValue();
    double highThresh = highThreshSocket.getValue().get().doubleValue();
    int apertureSize = apertureSizeSocket.getValue().get().intValue();
    boolean l2gradient = l2gradientSocket.getValue().get();
    if (preferCuda()) {
      try (CannyEdgeDetector cannyEdgeDetector = createCannyEdgeDetector(
          lowThresh,
          highThresh,
          apertureSize,
          l2gradient)) {
        cannyEdgeDetector.detect(inputSocket.getValue().get().getGpu(),
            outputSocket.getValue().get().rawGpu());
      }
    } else {
      Canny(inputSocket.getValue().get().getCpu(), outputSocket.getValue().get().getCpu(),
          lowThresh, highThresh, apertureSize, l2gradient);
    }
    outputSocket.flagChanged();
  }
}
