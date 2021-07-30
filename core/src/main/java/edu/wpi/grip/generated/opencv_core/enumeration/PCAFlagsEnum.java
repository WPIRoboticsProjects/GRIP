package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.opencv_core.PCA;

public enum PCAFlagsEnum {

  /**
   * indicates that the input samples are stored as matrix rows
   */
  DATA_AS_ROW(PCA.DATA_AS_ROW),
  /**
   * indicates that the input samples are stored as matrix columns
   */
  DATA_AS_COL(PCA.DATA_AS_COL), USE_AVG(PCA.USE_AVG);

  public final int value;

  PCAFlagsEnum(int value) {
    this.value = value;
  }
}
