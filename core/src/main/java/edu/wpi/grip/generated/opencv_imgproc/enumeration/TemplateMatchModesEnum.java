package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum TemplateMatchModesEnum {

  /**
   * \f[R(x,y)= \sum _{x',y'} (T(x',y')-I(x+x',y+y'))^2\f]
   */
  TM_SQDIFF(opencv_imgproc.TM_SQDIFF),
  /**
   * \f[R(x,y)= \frac{\sum_{x',y'} (T(x',y')-I(x+x',y+y'))^2}{\sqrt{\sum_{x',y'}T(x',y')^2 \cdot
   * \sum_{x',y'} I(x+x',y+y')^2}}\f]
   */
  TM_SQDIFF_NORMED(opencv_imgproc.TM_SQDIFF_NORMED),
  /**
   * \f[R(x,y)= \sum _{x',y'} (T(x',y')  \cdot I(x+x',y+y'))\f]
   */
  TM_CCORR(opencv_imgproc.TM_CCORR),
  /**
   * \f[R(x,y)= \frac{\sum_{x',y'} (T(x',y') \cdot I(x+x',y+y'))}{\sqrt{\sum_{x',y'}T(x',y')^2 \cdot
   * \sum_{x',y'} I(x+x',y+y')^2}}\f]
   */
  TM_CCORR_NORMED(opencv_imgproc.TM_CCORR_NORMED),
  /**
   * \f[R(x,y)= \sum _{x',y'} (T'(x',y')  \cdot I'(x+x',y+y'))\f] where \f[\begin{array}{l}
   * T'(x',y')=T(x',y') - 1/(w  \cdot h)  \cdot \sum _{x'',y''} T(x'',y'') \\
   * I'(x+x',y+y')=I(x+x',y+y') - 1/(w  \cdot h)  \cdot \sum _{x'',y''} I(x+x'',y+y'')
   * \end{array}\f]
   */
  TM_CCOEFF(opencv_imgproc.TM_CCOEFF),
  /**
   * \f[R(x,y)= \frac{ \sum_{x',y'} (T'(x',y') \cdot I'(x+x',y+y')) }{ \sqrt{\sum_{x',y'}T'(x',y')^2
   * \cdot \sum_{x',y'} I'(x+x',y+y')^2} }\f]
   */
  TM_CCOEFF_NORMED(opencv_imgproc.TM_CCOEFF_NORMED);

  public final int value;

  TemplateMatchModesEnum(int value) {
    this.value = value;
  }
}
