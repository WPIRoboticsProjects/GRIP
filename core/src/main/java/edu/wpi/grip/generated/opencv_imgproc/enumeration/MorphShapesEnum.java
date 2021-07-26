package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum MorphShapesEnum {

  /**
   * a rectangular structuring element:  \f[E_{ij}=1\f]
   */
  MORPH_RECT(opencv_imgproc.MORPH_RECT),
  /**
   * a cross-shaped structuring element: \f[E_{ij} =  \fork{1}{if i=\texttt{anchor.y} or
   * j=\texttt{anchor.x}}{0}{otherwise}\f]
   */
  MORPH_CROSS(opencv_imgproc.MORPH_CROSS),
  /**
   * an elliptic structuring element, that is, a filled ellipse inscribed into the rectangle Rect(0,
   * 0, esize.width, 0.esize.height)
   */
  MORPH_ELLIPSE(opencv_imgproc.MORPH_ELLIPSE);

  public final int value;

  MorphShapesEnum(int value) {
    this.value = value;
  }
}
