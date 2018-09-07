package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum oclKernelArgEnum {

    LOCAL(opencv_core.KernelArg.LOCAL), READ_ONLY(opencv_core.KernelArg.READ_ONLY), WRITE_ONLY(opencv_core.KernelArg.WRITE_ONLY), READ_WRITE(opencv_core.KernelArg.READ_WRITE), CONSTANT(opencv_core.KernelArg.CONSTANT), PTR_ONLY(opencv_core.KernelArg.PTR_ONLY), NO_SIZE(opencv_core.KernelArg.NO_SIZE);

    public final int value;

    oclKernelArgEnum(int value) {
        this.value = value;
    }
}
