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

import org.bytedeco.opencv.opencv_core.GpuMat;
import org.bytedeco.opencv.global.opencv_cudaimgproc;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC4;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_cudafilters.Filter;
import static org.bytedeco.opencv.global.opencv_cudafilters.createGaussianFilter;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2BGRA;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGRA2BGR;
import static org.bytedeco.opencv.global.opencv_imgproc.GaussianBlur;
import static org.bytedeco.opencv.global.opencv_imgproc.bilateralFilter;
import static org.bytedeco.opencv.global.opencv_imgproc.blur;
import static org.bytedeco.opencv.global.opencv_imgproc.medianBlur;

/**
 * An {@link Operation} that softens an image using one of several different filters.
 */
@Description(name = "Blur",
             summary = "Blurs an image to remove noise",
             category = OperationCategory.IMAGE_PROCESSING,
             iconName = "blur")
public class BlurOperation extends CudaOperation {

  private final SocketHint<Type> typeHint = SocketHints.createEnumSocketHint("Type", Type.BOX);
  private final SocketHint<Number> radiusHint = SocketHints.Inputs
      .createNumberSliderSocketHint("Radius", 0.0, 0.0, 100.0);
  private final InputSocket<Type> typeSocket;
  private final InputSocket<Number> radiusSocket;

  private int lastKernelSize = 0;
  // used to covert 3-channel images to 4-channel for CUDA
  private final GpuMat upcast = new GpuMat();
  private Filter gpuGaussianFilter;
  //private Filter gpuMedianFilter;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public BlurOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    super(inputSocketFactory, outputSocketFactory);
    this.typeSocket = inputSocketFactory.create(typeHint);
    this.radiusSocket = inputSocketFactory.create(radiusHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        typeSocket,
        radiusSocket,
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
  @SuppressWarnings("PMD.ExcessiveMethodLength")
  public void perform() {
    final MatWrapper input = inputSocket.getValue().get();
    if (input.empty()) {
      return;
    }
    final Type type = typeSocket.getValue().get();
    final Number radius = radiusSocket.getValue().get();

    final MatWrapper output = outputSocket.getValue().get();

    int imageType;
    int kernelSize;
    boolean kernelChange;

    if (preferCuda()) {
      if (input.type() == CV_8UC3) {
        // GPU filters generally don't take BGR images, but will take BGRA
        // So we convert the BGR image to BGRA here and convert it back at the end
        // Note that this doesn't care about the actual pixel format because we're
        // converting to the same format, just with an extra channel
        imageType = CV_8UC4;
        opencv_cudaimgproc.cvtColor(input.getGpu(), upcast, CV_BGR2BGRA);
        gpuIn.put(upcast);
      } else {
        input.copyTo(gpuIn);
        imageType = input.type();
      }
    } else {
      imageType = input.type();
    }

    switch (type) {
      case BOX:
        // Box filter kernels must have an odd size
        kernelSize = 2 * radius.intValue() + 1;

        // Don't bother with CUDA acceleration here; CPU is fast enough that memory copies
        // will remove the speedups from CUDA
        blur(input.getCpu(), output.getCpu(), new Size(kernelSize, kernelSize));
        break;

      case GAUSSIAN:
        // A Gaussian blur radius is a standard deviation, so a kernel that extends three radii
        // in either direction from the center should account for 99.7% of the theoretical
        // influence on each pixel.
        kernelSize = 6 * radius.intValue() + 1;
        kernelChange = kernelSize != lastKernelSize;
        lastKernelSize = kernelSize;
        if (preferCuda()/* && kernelSize < 32*/) {
          // GPU gaussian blurs require kernel size in 0..31
          if (kernelChange || gpuGaussianFilter == null) {
            gpuGaussianFilter = createGaussianFilter(imageType, imageType,
                new Size(kernelSize, kernelSize), radius.doubleValue());
          }
          gpuGaussianFilter.apply(gpuIn, gpuOut);
          output.set(gpuOut);
        } else {
          GaussianBlur(input.getCpu(), output.getCpu(), new Size(kernelSize, kernelSize),
              radius.doubleValue());
        }
        break;

      case MEDIAN:
        kernelSize = 2 * radius.intValue() + 1;
        // FIXME: CUDA median filters is broken - run on CPU only for now
        /*
        kernelChange = kernelSize != lastKernelSize;
        lastKernelSize = kernelSize;
        if (preferCuda() && imageType == CV_8UC1) {
          // GPU median filters only work on grayscale images
          if (kernelChange || gpuMedianFilter == null) {
            gpuMedianFilter = createMedianFilter(imageType, kernelSize);
          }
          gpuMedianFilter.apply(gpuIn, gpuOut);
          output.set(gpuOut);
        } else {
          medianBlur(input.getCpu(), output.rawCpu(), kernelSize);
        }
        */
        medianBlur(input.getCpu(), output.rawCpu(), kernelSize);
        break;

      case BILATERAL_FILTER:
        if (preferCuda()) {
          opencv_cudaimgproc.bilateralFilter(gpuIn, gpuOut,
              -1, radius.floatValue(), radius.floatValue() / 6);
          output.set(gpuOut);
        } else {
          bilateralFilter(input.getCpu(), output.rawCpu(),
              -1, radius.doubleValue(), radius.doubleValue() / 6);
        }
        break;

      default:
        throw new IllegalArgumentException("Illegal blur type: " + type);
    }

    if (preferCuda() && output.type() == CV_8UC4 && input.type() == CV_8UC3) {
      // Remove the alpha channel that was added for GPU filtering
      opencv_cudaimgproc.cvtColor(output.getGpu(), output.rawGpu(), CV_BGRA2BGR);
    }

    //output.set(output);
    outputSocket.setValue(output);
  }

  private enum Type {
    BOX("Box Blur"),
    GAUSSIAN("Gaussian Blur"),
    MEDIAN("Median Filter"),
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
