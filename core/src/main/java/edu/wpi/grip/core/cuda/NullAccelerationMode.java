package edu.wpi.grip.core.cuda;

public class NullAccelerationMode implements AccelerationMode {
  @Override
  public boolean isUsingCuda() {
    return false;
  }
}
