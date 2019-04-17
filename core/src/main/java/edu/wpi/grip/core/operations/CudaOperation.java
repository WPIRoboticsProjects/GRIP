package edu.wpi.grip.core.operations;

import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.CudaSocket;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import org.bytedeco.javacpp.opencv_core.GpuMat;
import org.bytedeco.javacpp.opencv_core.Mat;

/**
 * A partial implementation of Operation that has the option to use CUDA acceleration.
 */
public abstract class CudaOperation implements Operation {

  protected final SocketHint<MatWrapper> inputHint =
      SocketHints.createImageSocketHint("Input");
  protected final SocketHint<Boolean> gpuHint =
      SocketHints.createBooleanSocketHint("Prefer GPU", false);
  protected final SocketHint<MatWrapper> outputHint = SocketHints.createImageSocketHint("Output");

  /**
   * Default image input socket.
   */
  protected final InputSocket<MatWrapper> inputSocket;
  /**
   * Input socket telling the operation to prefer to use CUDA acceleration when possible.
   */
  protected final CudaSocket gpuSocket;
  /**
   * Default image output socket.
   */
  protected final OutputSocket<MatWrapper> outputSocket;

  /**
   * The mat used for an input to the CPU operation.
   */
  protected final Mat cpuIn = new Mat();

  /**
   * The mat used for an input to the CUDA operation.
   */
  protected final GpuMat gpuIn = new GpuMat();

  /**
   * The output mat of a CPU operation.
   */
  protected final Mat cpuOut = new Mat();

  /**
   * The output mat of a CUDA operation.
   */
  protected final GpuMat gpuOut = new GpuMat();

  protected CudaOperation(InputSocket.Factory isf, OutputSocket.Factory osf) {
    inputSocket = isf.create(inputHint);
    gpuSocket = isf.createCuda(gpuHint);
    outputSocket = osf.create(outputHint);

    inputSocket.setValue(MatWrapper.using(cpuIn, gpuIn));
    outputSocket.setValue(MatWrapper.using(cpuOut, gpuOut));
  }

  @Override
  public void cleanUp() {
    cpuIn.deallocate();
    gpuIn.deallocate();
    cpuOut.deallocate();
    gpuOut.deallocate();
  }

  /**
   * Checks the {@link #gpuSocket} to see if this operation should prefer to use the CUDA codepath.
   *
   * @return true if this operation should prefer to use CUDA, false if it should only use the CPU
   */
  protected boolean preferCuda() {
    return gpuSocket.isCudaAvailable()
        && gpuSocket.getValue().orElse(false);
  }

}
