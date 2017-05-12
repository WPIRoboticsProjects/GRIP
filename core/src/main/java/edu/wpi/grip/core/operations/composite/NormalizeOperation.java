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

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.NORM_INF;
import static org.bytedeco.javacpp.opencv_core.NORM_L1;
import static org.bytedeco.javacpp.opencv_core.NORM_L2;
import static org.bytedeco.javacpp.opencv_core.NORM_MINMAX;
import static org.bytedeco.javacpp.opencv_core.normalize;

/**
 * GRIP {@link Operation} for {@link org.bytedeco.javacpp.opencv_core#normalize}.
 */
@Description(name = "Normalize",
             summary = "Normalizes or remaps the values of pixels in an image",
             category = OperationCategory.IMAGE_PROCESSING,
             iconName = "opencv")
public class NormalizeOperation implements Operation {

  private final SocketHint<MatWrapper> srcHint = SocketHints.createImageSocketHint("Input");
  private final SocketHint<Type> typeHint = SocketHints.createEnumSocketHint("Type", Type.MINMAX);
  private final SocketHint<Number> aHint = SocketHints.Inputs
      .createNumberSpinnerSocketHint("Alpha", 0.0, 0, Double.MAX_VALUE);
  private final SocketHint<Number> bHint = SocketHints.Inputs
      .createNumberSpinnerSocketHint("Beta", 255, 0, Double.MAX_VALUE);
  private final SocketHint<MatWrapper> dstHint = SocketHints.createImageSocketHint("Output");
  private final InputSocket<MatWrapper> srcSocket;
  private final InputSocket<Type> typeSocket;
  private final InputSocket<Number> alphaSocket;
  private final InputSocket<Number> betaSocket;
  private final OutputSocket<MatWrapper> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public NormalizeOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.srcSocket = inputSocketFactory.create(srcHint);
    this.typeSocket = inputSocketFactory.create(typeHint);
    this.alphaSocket = inputSocketFactory.create(aHint);
    this.betaSocket = inputSocketFactory.create(bHint);

    this.outputSocket = outputSocketFactory.create(dstHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        srcSocket,
        typeSocket,
        alphaSocket,
        betaSocket
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
    final Mat input = srcSocket.getValue().get().getCpu();
    final Type type = typeSocket.getValue().get();
    final Number a = alphaSocket.getValue().get();
    final Number b = betaSocket.getValue().get();

    final Mat output = outputSocket.getValue().get().rawCpu();

    normalize(input, output, a.doubleValue(), b.doubleValue(), type.value, -1, null);

    outputSocket.setValueOptional(outputSocket.getValue());
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
