package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum instrIMPLEnum {

    IMPL_PLAIN(opencv_core.IMPL_PLAIN), IMPL_IPP(opencv_core.IMPL_IPP), IMPL_OPENCL(opencv_core.IMPL_OPENCL);

    public final int value;

    instrIMPLEnum(int value) {
        this.value = value;
    }
}
