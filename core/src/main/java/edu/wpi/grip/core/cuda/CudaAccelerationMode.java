package edu.wpi.grip.core.cuda;

public class CudaAccelerationMode implements AccelerationMode {
  @Override
  public boolean isUsingCuda() {
    return true;
  }
}
