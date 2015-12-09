package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum HammingEnum {

    normType(opencv_core.Hamming.normType);

    public final int value;

    HammingEnum(int value) {
        this.value = value;
    }
}
