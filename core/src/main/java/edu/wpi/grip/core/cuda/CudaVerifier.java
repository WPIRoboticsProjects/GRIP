package edu.wpi.grip.core.cuda;

import edu.wpi.grip.core.util.SafeShutdown;

import com.google.common.annotations.VisibleForTesting;

import java.util.logging.Logger;

import javax.inject.Inject;

public class CudaVerifier {

  private static final Logger logger = Logger.getLogger(CudaVerifier.class.getName());

  private final AccelerationMode accelerationMode;
  private final CudaDetector cudaDetector;

  @Inject
  public CudaVerifier(AccelerationMode accelerationMode, CudaDetector cudaDetector) {
    this.accelerationMode = accelerationMode;
    this.cudaDetector = cudaDetector;
  }

  /**
   * Verifies the presence of a CUDA runtime, if required by the GRIP runtime, and exits the
   * app if no compatible CUDA runtime is available.
   */
  public void verifyCuda() {
    if (!verify()) {
      String message = "This version of GRIP requires CUDA "
          + CudaDetector.REQUIRED_VERSION
          + " to be installed and an NVIDIA graphics card in your computer. "
          + "If your computer does not have an NVIDIA graphics card, use a version of GRIP "
          + "without CUDA acceleration. Otherwise, you need to install the appropriate CUDA "
          + "runtime for your computer.";
      logger.severe(message);
      exit();
    }
  }

  /**
   * Verifies that, if GRIP is using CUDA acceleration, a compatible CUDA runtime is available. If
   * GRIP is not using CUDA acceleration, this will always return {@code true}.
   *
   * @return false if GRIP is using CUDA acceleration but no compatible CUDA runtime is available,
   *         true otherwise
   */
  public boolean verify() {
    if (accelerationMode.isUsingCuda()) {
      return cudaDetector.isCompatibleCudaInstalled();
    } else {
      return true;
    }
  }

  /**
   * Exits the application.
   */
  @VisibleForTesting
  void exit() {
    SafeShutdown.exit(SafeShutdown.ExitCodes.CUDA_UNAVAILABLE);
  }
}
