package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum cudaHostMemEnum {

    PAGE_LOCKED(opencv_core.HostMem.PAGE_LOCKED), SHARED(opencv_core.HostMem.SHARED), WRITE_COMBINED(opencv_core.HostMem.WRITE_COMBINED);

    public final int value;

    cudaHostMemEnum(int value) {
        this.value = value;
    }
}
