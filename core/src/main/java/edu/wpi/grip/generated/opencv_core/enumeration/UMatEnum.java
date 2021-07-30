package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.opencv_core.UMat;

public enum UMatEnum {

  MAGIC_VAL(UMat.MAGIC_VAL), AUTO_STEP(UMat.AUTO_STEP), CONTINUOUS_FLAG(UMat.CONTINUOUS_FLAG), SUBMATRIX_FLAG(UMat.SUBMATRIX_FLAG), MAGIC_MASK(UMat.MAGIC_MASK), TYPE_MASK(UMat.TYPE_MASK), DEPTH_MASK(UMat.DEPTH_MASK);

  public final int value;

  UMatEnum(int value) {
    this.value = value;
  }
}
