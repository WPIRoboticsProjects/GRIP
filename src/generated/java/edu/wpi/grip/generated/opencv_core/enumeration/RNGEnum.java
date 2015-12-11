package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum RNGEnum {

    UNIFORM(opencv_core.RNG.UNIFORM), NORMAL(opencv_core.RNG.NORMAL);

    public final int value;

    RNGEnum(int value) {
        this.value = value;
    }
}
