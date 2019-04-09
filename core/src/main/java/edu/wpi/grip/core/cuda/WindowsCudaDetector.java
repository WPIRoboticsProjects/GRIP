package edu.wpi.grip.core.cuda;

public class WindowsCudaDetector implements CudaDetector {
  @Override
  public boolean isCompatibleCudaInstalled() {
    return false;
  }
}
