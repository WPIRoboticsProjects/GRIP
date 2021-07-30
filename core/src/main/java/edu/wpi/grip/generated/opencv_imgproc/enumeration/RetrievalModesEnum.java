package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum RetrievalModesEnum {

  /**
   * retrieves only the extreme outer contours. It sets `hierarchy[i][2]=hierarchy[i][3]=-1` for all
   * the contours.
   */
  RETR_EXTERNAL(opencv_imgproc.RETR_EXTERNAL),
  /**
   * retrieves all of the contours without establishing any hierarchical relationships.
   */
  RETR_LIST(opencv_imgproc.RETR_LIST),
  /**
   * retrieves all of the contours and organizes them into a two-level hierarchy. At the top level,
   * there are external boundaries of the components. At the second level, there are boundaries of
   * the holes. If there is another contour inside a hole of a connected component, it is still put
   * at the top level.
   */
  RETR_CCOMP(opencv_imgproc.RETR_CCOMP),
  /**
   * retrieves all of the contours and reconstructs a full hierarchy of nested contours.
   */
  RETR_TREE(opencv_imgproc.RETR_TREE), RETR_FLOODFILL(opencv_imgproc.RETR_FLOODFILL);

  public final int value;

  RetrievalModesEnum(int value) {
    this.value = value;
  }
}
