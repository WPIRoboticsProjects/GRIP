package edu.wpi.grip.core.cuda;

import edu.wpi.grip.core.util.SafeShutdown;

import com.google.common.annotations.VisibleForTesting;

import java.util.Properties;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;

public class CudaVerifier {

  private static final Logger logger = Logger.getLogger(CudaVerifier.class.getName());

  private static final String CUDA_VERSION_KEY = "edu.wpi.grip.cuda.version";

  private final AccelerationMode accelerationMode;
  private final CudaDetector cudaDetector;
  private final String cudaVersion;

  @Inject
  public CudaVerifier(AccelerationMode accelerationMode,
                      CudaDetector cudaDetector,
                      @Named("cudaProperties") Properties cudaProperties) {
    this.accelerationMode = accelerationMode;
    this.cudaDetector = cudaDetector;
    this.cudaVersion = cudaProperties.getProperty(CUDA_VERSION_KEY, "Unknown");
  }

  /**
   * Verifies the presence of a CUDA runtime, if required by the GRIP runtime, and exits the app if
   * no compatible CUDA runtime is available.
   */
  public void verifyCuda() {
    if (!verify()) {
      String message = "This version of GRIP requires CUDA version "
          + cudaVersion
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
   *     true otherwise
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
    SafeShutdown.exit(SafeShutdown.ExitCode.CUDA_UNAVAILABLE);
  }
}
