package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.operations.CudaOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.bytedeco.opencv.global.opencv_cudaimgproc;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGRA2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

/**
 * An {@link Operation} that converts a color image into shades of gray.
 */
@Description(name = "Desaturate",
             summary = "Convert a color image into shades of gray",
             category = OperationCategory.IMAGE_PROCESSING,
             iconName = "desaturate")
public class DesaturateOperation extends CudaOperation {

  @Inject
  @SuppressWarnings("JavadocMethod")
  public DesaturateOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    super(inputSocketFactory, outputSocketFactory);
  }


  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
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
    final MatWrapper output = outputSocket.getValue().get();


    switch (input.channels()) {
      case 1:
        // If the input is already one channel, it's already desaturated
        output.set(input);
        break;

      case 3:
        if (preferCuda()) {
          opencv_cudaimgproc.cvtColor(input.getGpu(), output.rawGpu(), COLOR_BGR2GRAY);
        } else {
          cvtColor(input.getCpu(), output.rawCpu(), COLOR_BGR2GRAY);
        }
        break;

      case 4:
        if (preferCuda()) {
          opencv_cudaimgproc.cvtColor(input.getGpu(), output.rawGpu(), COLOR_BGRA2GRAY);
        } else {
          cvtColor(input.getCpu(), output.rawCpu(), COLOR_BGRA2GRAY);
        }
        break;

      default:
        throw new IllegalArgumentException("Input to desaturate must have 1, 3, or 4 channels");
    }

    outputSocket.setValue(output);
  }
}
