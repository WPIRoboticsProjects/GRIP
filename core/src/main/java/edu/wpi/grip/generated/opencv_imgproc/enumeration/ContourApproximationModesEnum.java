package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum ContourApproximationModesEnum {

  /**
   * stores absolutely all the contour points. That is, any 2 subsequent points (x1,y1) and (x2,y2)
   * of the contour will be either horizontal, vertical or diagonal neighbors, that is,
   * max(abs(x1-x2),abs(y2-y1))==1.
   */
  CHAIN_APPROX_NONE(opencv_imgproc.CHAIN_APPROX_NONE),
  /**
   * compresses horizontal, vertical, and diagonal segments and leaves only their end points. For
   * example, an up-right rectangular contour is encoded with 4 points.
   */
  CHAIN_APPROX_SIMPLE(opencv_imgproc.CHAIN_APPROX_SIMPLE),
  /**
   * applies one of the flavors of the Teh-Chin chain approximation algorithm @cite TehChin89
   */
  CHAIN_APPROX_TC89_L1(opencv_imgproc.CHAIN_APPROX_TC89_L1),
  /**
   * applies one of the flavors of the Teh-Chin chain approximation algorithm @cite TehChin89
   */
  CHAIN_APPROX_TC89_KCOS(opencv_imgproc.CHAIN_APPROX_TC89_KCOS);

  public final int value;

  ContourApproximationModesEnum(int value) {
    this.value = value;
  }
}
