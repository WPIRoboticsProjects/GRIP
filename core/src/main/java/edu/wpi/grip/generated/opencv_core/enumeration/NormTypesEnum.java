package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.global.opencv_core;

public enum NormTypesEnum {
  NORM_INF(opencv_core.NORM_INF), NORM_L1(opencv_core.NORM_L1), NORM_L2(opencv_core.NORM_L2), NORM_L2SQR(opencv_core.NORM_L2SQR), NORM_HAMMING(opencv_core.NORM_HAMMING), NORM_HAMMING2(opencv_core.NORM_HAMMING2), NORM_TYPE_MASK(opencv_core.NORM_TYPE_MASK),
  /**
   * flag
   */
  NORM_RELATIVE(opencv_core.NORM_RELATIVE),
  /**
   * flag
   */
  NORM_MINMAX(opencv_core.NORM_MINMAX);

  public final int value;

  NormTypesEnum(int value) {
    this.value = value;
  }
}
