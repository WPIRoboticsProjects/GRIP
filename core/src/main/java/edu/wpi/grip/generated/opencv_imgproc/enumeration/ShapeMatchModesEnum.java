package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.javacpp.opencv_imgproc;

public enum ShapeMatchModesEnum {

    CONTOURS_MATCH_I1(opencv_imgproc.CONTOURS_MATCH_I1), CONTOURS_MATCH_I2(opencv_imgproc.CONTOURS_MATCH_I2), CONTOURS_MATCH_I3(opencv_imgproc.CONTOURS_MATCH_I3);

    public final int value;

    ShapeMatchModesEnum(int value) {
        this.value = value;
    }
}
