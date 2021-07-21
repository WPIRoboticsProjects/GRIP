package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum LineTypesEnum {

    FILLED(opencv_imgproc.FILLED), /** 4-connected line */
    LINE_4(opencv_imgproc.LINE_4), /** 8-connected line */
    LINE_8(opencv_imgproc.LINE_8), /** antialiased line */
    LINE_AA(opencv_imgproc.LINE_AA);

    public final int value;

    LineTypesEnum(int value) {
        this.value = value;
    }
}
