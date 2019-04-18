package edu.wpi.grip.core.cuda;

public class NullCudaDetector implements CudaDetector {
  @Override
  public boolean isCompatibleCudaInstalled() {
    return false;
  }
}
