package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.operations.CudaOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_cudaarithm;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.NORM_INF;
import static org.bytedeco.opencv.global.opencv_core.NORM_L1;
import static org.bytedeco.opencv.global.opencv_core.NORM_L2;
import static org.bytedeco.opencv.global.opencv_core.NORM_MINMAX;

/**
 * GRIP {@link Operation} for {@link org.bytedeco.opencv.global.opencv_core#normalize}.
 */
@Description(name = "Normalize",
    summary = "Normalizes or remaps the values of pixels in an image",
    category = OperationCategory.IMAGE_PROCESSING,
    iconName = "opencv")
public class NormalizeOperation extends CudaOperation {

  private final SocketHint<Type> typeHint = SocketHints.createEnumSocketHint("Type", Type.MINMAX);
  private final SocketHint<Number> aHint = SocketHints.Inputs
      .createNumberSpinnerSocketHint("Alpha", 0.0, 0, Double.MAX_VALUE);
  private final SocketHint<Number> bHint = SocketHints.Inputs
      .createNumberSpinnerSocketHint("Beta", 255, 0, Double.MAX_VALUE);
  private final InputSocket<Type> typeSocket;
  private final InputSocket<Number> alphaSocket;
  private final InputSocket<Number> betaSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public NormalizeOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    super(inputSocketFactory, outputSocketFactory);
    this.typeSocket = inputSocketFactory.create(typeHint);
    this.alphaSocket = inputSocketFactory.create(aHint);
    this.betaSocket = inputSocketFactory.create(bHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        typeSocket,
        alphaSocket,
        betaSocket,
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
  public void perform() {
    final MatWrapper input = inputSocket.getValue().get();
    final int type = typeSocket.getValue().get().value;
    final double a = alphaSocket.getValue().get().doubleValue();
    final double b = betaSocket.getValue().get().doubleValue();

    final MatWrapper output = outputSocket.getValue().get();

    if (preferCuda() && input.channels() == 1) {
      // CUDA normalize only works on single-channel images
      opencv_cudaarithm.normalize(input.getGpu(), output.rawGpu(), a, b, type, -1);
    } else {
      opencv_core.normalize(input.getCpu(), output.rawCpu(), a, b, type, -1, null);
    }

    outputSocket.flagChanged();
  }

  private enum Type {

    INF("NORM_INF", NORM_INF),
    L1("NORM_L1", NORM_L1),
    L2("NORM_L2", NORM_L2),
    MINMAX("NORM_MINMAX", NORM_MINMAX);

    private final String label;
    private final int value;

    Type(String name, int value) {
      this.label = name;
      this.value = value;
    }

    @Override
    public String toString() {
      return label;
    }

  }

}
