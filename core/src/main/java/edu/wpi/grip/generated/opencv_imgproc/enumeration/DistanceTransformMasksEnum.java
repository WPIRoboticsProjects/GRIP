package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum DistanceTransformMasksEnum {

  /**
   * mask=3
   */
  DIST_MASK_3(opencv_imgproc.DIST_MASK_3),
  /**
   * mask=5
   */
  DIST_MASK_5(opencv_imgproc.DIST_MASK_5), DIST_MASK_PRECISE(opencv_imgproc.DIST_MASK_PRECISE);

  public final int value;

  DistanceTransformMasksEnum(int value) {
    this.value = value;
  }
}
