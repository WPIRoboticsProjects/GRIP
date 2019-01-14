package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum LineTypesEnum {

    FILLED(opencv_core.FILLED), /** 4-connected line */
    LINE_4(opencv_core.LINE_4), /** 8-connected line */
    LINE_8(opencv_core.LINE_8), /** antialiased line */
    LINE_AA(opencv_core.LINE_AA);

    public final int value;

    LineTypesEnum(int value) {
        this.value = value;
    }
}
