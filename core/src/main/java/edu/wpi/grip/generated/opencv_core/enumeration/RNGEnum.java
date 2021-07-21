package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.opencv_core.RNG;

public enum RNGEnum {

    UNIFORM(RNG.UNIFORM), NORMAL(RNG.NORMAL);

    public final int value;

    RNGEnum(int value) {
        this.value = value;
    }
}
