package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.global.opencv_core;

public enum UMatUsageFlagsEnum {

  USAGE_DEFAULT(opencv_core.USAGE_DEFAULT), USAGE_ALLOCATE_HOST_MEMORY(opencv_core.USAGE_ALLOCATE_HOST_MEMORY), USAGE_ALLOCATE_DEVICE_MEMORY(opencv_core.USAGE_ALLOCATE_DEVICE_MEMORY), USAGE_ALLOCATE_SHARED_MEMORY(opencv_core.USAGE_ALLOCATE_SHARED_MEMORY), __UMAT_USAGE_FLAGS_32BIT(opencv_core.__UMAT_USAGE_FLAGS_32BIT);

  public final int value;

  UMatUsageFlagsEnum(int value) {
    this.value = value;
  }
}
