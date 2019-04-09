package edu.wpi.grip.core.cuda;

/**
 * Detects CUDA installs.
 */
public interface CudaDetector {

  String REQUIRED_VERSION = "10.0";

  /**
   * Checks if a CUDA runtime is installed that is compatible with what we need for OpenCV.
   */
  boolean isCompatibleCudaInstalled();

}
