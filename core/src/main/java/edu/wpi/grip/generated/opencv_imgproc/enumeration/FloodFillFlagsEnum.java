package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum FloodFillFlagsEnum {

  /**
   * If set, the difference between the current pixel and seed pixel is considered. Otherwise, the
   * difference between neighbor pixels is considered (that is, the range is floating).
   */
  FLOODFILL_FIXED_RANGE(opencv_imgproc.FLOODFILL_FIXED_RANGE),
  /**
   * If set, the function does not change the image ( newVal is ignored), and only fills the mask
   * with the value specified in bits 8-16 of flags as described above. This option only make sense
   * in function variants that have the mask parameter.
   */
  FLOODFILL_MASK_ONLY(opencv_imgproc.FLOODFILL_MASK_ONLY);

  public final int value;

  FloodFillFlagsEnum(int value) {
    this.value = value;
  }
}
