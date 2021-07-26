package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import static org.bytedeco.opencv.global.opencv_imgproc.INTER_AREA;
import static org.bytedeco.opencv.global.opencv_imgproc.INTER_CUBIC;
import static org.bytedeco.opencv.global.opencv_imgproc.INTER_LANCZOS4;
import static org.bytedeco.opencv.global.opencv_imgproc.INTER_LINEAR;
import static org.bytedeco.opencv.global.opencv_imgproc.INTER_NEAREST;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * Scale an image to an exact width and height using one of several interpolation modes.  Scaling
 * images down can be a useful optimization, and scaling them up might be necessary for combining
 * multiple images that are different sizes.
 */
@Description(name = "Resize Image",
             summary = "Scale an image to an exact size",
             category = OperationCategory.IMAGE_PROCESSING,
             iconName = "resize")
public class ResizeOperation implements Operation {

  private final InputSocket<MatWrapper> inputSocket;
  private final InputSocket<Number> widthSocket;
  private final InputSocket<Number> heightSocket;
  private final InputSocket<Interpolation> interpolationSocket;

  private final OutputSocket<MatWrapper> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public ResizeOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.inputSocket = inputSocketFactory.create(SocketHints.createImageSocketHint("Input"));
    this.widthSocket = inputSocketFactory.create(SocketHints.Inputs
        .createNumberSpinnerSocketHint("Width", 640));
    this.heightSocket = inputSocketFactory.create(SocketHints.Inputs
        .createNumberSpinnerSocketHint("Height", 480));
    this.interpolationSocket = inputSocketFactory
        .create(SocketHints.createEnumSocketHint("Interpolation", Interpolation.CUBIC));

    this.outputSocket = outputSocketFactory.create(SocketHints.createImageSocketHint("Output"));
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        widthSocket,
        heightSocket,
        interpolationSocket
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
    final Number width = widthSocket.getValue().get();
    final Number height = heightSocket.getValue().get();
    final Interpolation interpolation = interpolationSocket.getValue().get();

    final Mat output = outputSocket.getValue().get().rawCpu();

    resize(input, output, new Size(width.intValue(), height.intValue()), 0.0, 0.0, interpolation
        .value);

    outputSocket.flagChanged();
  }

  private enum Interpolation {
    NEAREST("None", INTER_NEAREST),
    LINEAR("Linear", INTER_LINEAR),
    CUBIC("Cubic", INTER_CUBIC),
    LANCZOS("Lanczos", INTER_LANCZOS4),
    AREA("Area", INTER_AREA);

    final String label;
    final int value;

    Interpolation(String label, int value) {
      this.label = label;
      this.value = value;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
