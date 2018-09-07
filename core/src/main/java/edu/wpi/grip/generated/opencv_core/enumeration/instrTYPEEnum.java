package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum instrTYPEEnum {

    TYPE_GENERAL(opencv_core.TYPE_GENERAL), TYPE_MARKER(opencv_core.TYPE_MARKER), TYPE_WRAPPER(opencv_core.TYPE_WRAPPER), TYPE_FUN(opencv_core.TYPE_FUN);

    public final int value;

    instrTYPEEnum(int value) {
        this.value = value;
    }
}
