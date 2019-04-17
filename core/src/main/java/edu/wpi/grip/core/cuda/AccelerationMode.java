package edu.wpi.grip.core.cuda;

/**
 * App-wide hardware acceleration mode.
 */
public interface AccelerationMode {

  /**
   * Flag marking that GRIP is using CUDA-accelerated OpenCV.
   */
  boolean isUsingCuda();

}
