package edu.wpi.grip.core.cuda;

/**
 * Detects CUDA installs.
 */
public interface CudaDetector {

  /**
   * Checks if a CUDA runtime is installed that is compatible with what we need for OpenCV.
   */
  boolean isCompatibleCudaInstalled();

}
