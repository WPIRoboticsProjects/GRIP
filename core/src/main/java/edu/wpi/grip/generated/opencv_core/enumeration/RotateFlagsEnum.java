package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum RotateFlagsEnum {

    /**Rotate 90 degrees clockwise */
    ROTATE_90_CLOCKWISE(opencv_core.ROTATE_90_CLOCKWISE), /**Rotate 180 degrees clockwise */
    ROTATE_180(opencv_core.ROTATE_180), /**Rotate 270 degrees clockwise */
    ROTATE_90_COUNTERCLOCKWISE(opencv_core.ROTATE_90_COUNTERCLOCKWISE);

    public final int value;

    RotateFlagsEnum(int value) {
        this.value = value;
    }
}
