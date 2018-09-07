package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum UMatDataEnum {

    COPY_ON_MAP(opencv_core.UMatData.COPY_ON_MAP), HOST_COPY_OBSOLETE(opencv_core.UMatData.HOST_COPY_OBSOLETE), DEVICE_COPY_OBSOLETE(opencv_core.UMatData.DEVICE_COPY_OBSOLETE), TEMP_UMAT(opencv_core.UMatData.TEMP_UMAT), TEMP_COPIED_UMAT(opencv_core.UMatData.TEMP_COPIED_UMAT), USER_ALLOCATED(opencv_core.UMatData.USER_ALLOCATED), DEVICE_MEM_MAPPED(opencv_core.UMatData.DEVICE_MEM_MAPPED), ASYNC_CLEANUP(opencv_core.UMatData.ASYNC_CLEANUP);

    public final int value;

    UMatDataEnum(int value) {
        this.value = value;
    }
}
