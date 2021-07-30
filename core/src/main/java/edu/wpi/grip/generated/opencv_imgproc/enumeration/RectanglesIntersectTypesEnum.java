package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum RectanglesIntersectTypesEnum {

  /**
   * No intersection
   */
  INTERSECT_NONE(opencv_imgproc.INTERSECT_NONE),
  /**
   * There is a partial intersection
   */
  INTERSECT_PARTIAL(opencv_imgproc.INTERSECT_PARTIAL),
  /**
   * One of the rectangle is fully enclosed in the other
   */
  INTERSECT_FULL(opencv_imgproc.INTERSECT_FULL);

  public final int value;

  RectanglesIntersectTypesEnum(int value) {
    this.value = value;
  }
}
