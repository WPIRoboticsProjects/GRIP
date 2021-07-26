package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum LineSegmentDetectorModesEnum {

  /**
   * No refinement applied
   */
  LSD_REFINE_NONE(opencv_imgproc.LSD_REFINE_NONE),
  /**
   * Standard refinement is applied. E.g. breaking arches into smaller straighter line
   * approximations.
   */
  LSD_REFINE_STD(opencv_imgproc.LSD_REFINE_STD),
  /**
   * Advanced refinement. Number of false alarms is calculated, lines are refined through increase
   * of precision, decrement in size, etc.
   */
  LSD_REFINE_ADV(opencv_imgproc.LSD_REFINE_ADV);

  public final int value;

  LineSegmentDetectorModesEnum(int value) {
    this.value = value;
  }
}
