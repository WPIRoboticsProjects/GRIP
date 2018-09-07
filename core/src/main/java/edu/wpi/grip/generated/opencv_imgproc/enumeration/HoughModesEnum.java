package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.javacpp.opencv_imgproc;

public enum HoughModesEnum {

    /** classical or standard Hough transform. Every line is represented by two floating-point
    numbers \f$(\rho, \theta)\f$ , where \f$\rho\f$ is a distance between (0,0) point and the line,
    and \f$\theta\f$ is the angle between x-axis and the normal to the line. Thus, the matrix must
    be (the created sequence will be) of CV_32FC2 type */
    HOUGH_STANDARD(opencv_imgproc.HOUGH_STANDARD), /** probabilistic Hough transform (more efficient in case if the picture contains a few long
    linear segments). It returns line segments rather than the whole line. Each segment is
    represented by starting and ending points, and the matrix must be (the created sequence will
    be) of the CV_32SC4 type. */
    HOUGH_PROBABILISTIC(opencv_imgproc.HOUGH_PROBABILISTIC), /** multi-scale variant of the classical Hough transform. The lines are encoded the same way as
    HOUGH_STANDARD. */
    HOUGH_MULTI_SCALE(opencv_imgproc.HOUGH_MULTI_SCALE), /** basically *21HT*, described in \cite Yuen90 */
    HOUGH_GRADIENT(opencv_imgproc.HOUGH_GRADIENT);

    public final int value;

    HoughModesEnum(int value) {
        this.value = value;
    }
}
