package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum DistanceTransformLabelTypesEnum {

  /**
   * each connected component of zeros in src (as well as all the non-zero pixels closest to the
   * connected component) will be assigned the same label
   */
  DIST_LABEL_CCOMP(opencv_imgproc.DIST_LABEL_CCOMP),
  /**
   * each zero pixel (and all the non-zero pixels closest to it) gets its own label.
   */
  DIST_LABEL_PIXEL(opencv_imgproc.DIST_LABEL_PIXEL);

  public final int value;

  DistanceTransformLabelTypesEnum(int value) {
    this.value = value;
  }
}
