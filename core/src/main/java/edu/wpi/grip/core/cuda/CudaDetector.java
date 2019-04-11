package edu.wpi.grip.core.cuda;

/**
 * Detects CUDA installs.
 */
public interface CudaDetector {

  /**
   * The required major CUDA version. Other minor versions are allowed, as long as they are
   * compatible (eg CUDA 10.0, 10.1, etc. are all acceptable).
   */
  String REQUIRED_MAJOR_VERSION = "10";

  /**
   * Checks if a CUDA runtime is installed that is compatible with what we need for OpenCV.
   */
  boolean isCompatibleCudaInstalled();

}
