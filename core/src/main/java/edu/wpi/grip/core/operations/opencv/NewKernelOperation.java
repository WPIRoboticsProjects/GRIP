package edu.wpi.grip.core.operations.opencv;


import edu.wpi.grip.core.Description;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.inject.Inject;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_imgproc;
import org.python.google.common.collect.ImmutableList;

import java.util.List;

@Description(name = "New Kernel",
        summary = "Create a kernel of custom size",
        category = OperationDescription.Category.OPENCV,
        iconName = "kernel")
public class NewKernelOperation implements CVOperation {

  private final SocketHint<KernelEnum> typeHint = SocketHints.createEnumSocketHint("kernelType",
          KernelEnum.MORPH_RECT);
  private final SocketHint<Number> widthHint = SocketHints.Inputs
          .createNumberSpinnerSocketHint("width", 1, 1, Integer.MAX_VALUE);
  private final SocketHint<Number> heightHint = SocketHints.Inputs
          .createNumberSpinnerSocketHint("height", 1, 1, Integer.MAX_VALUE);
  private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("kernel");


  private final InputSocket<Number> widthSocket;
  private final InputSocket<Number> heightSocket;
  private final InputSocket<KernelEnum> typeSocket;

  private final OutputSocket<Mat> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public NewKernelOperation(InputSocket.Factory inputSocketFactory,
                            OutputSocket.Factory outputSocketFactory) {
    this.typeSocket = inputSocketFactory.create(typeHint);
    this.widthSocket = inputSocketFactory.create(widthHint);
    this.heightSocket = inputSocketFactory.create(heightHint);
    this.outputSocket = outputSocketFactory.create(outputHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
            typeSocket,
            widthSocket,
            heightSocket
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
    final int widthValue = widthSocket.getValue().get().intValue();
    final int heightValue = heightSocket.getValue().get().intValue();
    final int kernelType = typeSocket.getValue().get().value;

    outputSocket.setValue(opencv_imgproc.getStructuringElement(kernelType, new Size(widthValue,
                          heightValue)));
  }

  public enum KernelEnum {
    MORPH_RECT(0),
    MORPH_CROSS(1),
    MORPH_ELLIPSE(2);

    public final int value;

    KernelEnum(int value) {
      this.value = value;
    }
  }
}

