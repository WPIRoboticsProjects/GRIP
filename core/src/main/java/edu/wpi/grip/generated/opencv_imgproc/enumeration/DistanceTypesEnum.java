package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum DistanceTypesEnum {

    DIST_USER(opencv_imgproc.DIST_USER), DIST_L1(opencv_imgproc.DIST_L1), DIST_L2(opencv_imgproc.DIST_L2), DIST_C(opencv_imgproc.DIST_C), DIST_L12(opencv_imgproc.DIST_L12), DIST_FAIR(opencv_imgproc.DIST_FAIR), DIST_WELSCH(opencv_imgproc.DIST_WELSCH), DIST_HUBER(opencv_imgproc.DIST_HUBER);

    public final int value;

    DistanceTypesEnum(int value) {
        this.value = value;
    }
}
