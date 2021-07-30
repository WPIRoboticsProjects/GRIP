package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.opencv_core.Mat;

public enum MatEnum {

  MAGIC_VAL(Mat.MAGIC_VAL), AUTO_STEP(Mat.AUTO_STEP), CONTINUOUS_FLAG(Mat.CONTINUOUS_FLAG), SUBMATRIX_FLAG(Mat.SUBMATRIX_FLAG), MAGIC_MASK(Mat.MAGIC_MASK), TYPE_MASK(Mat.TYPE_MASK), DEPTH_MASK(Mat.DEPTH_MASK);

  public final int value;

  MatEnum(int value) {
    this.value = value;
  }
}
