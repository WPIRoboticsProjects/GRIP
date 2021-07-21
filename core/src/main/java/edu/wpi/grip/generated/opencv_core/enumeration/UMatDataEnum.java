package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.opencv_core.UMatData;

public enum UMatDataEnum {

    COPY_ON_MAP(UMatData.COPY_ON_MAP), HOST_COPY_OBSOLETE(UMatData.HOST_COPY_OBSOLETE), DEVICE_COPY_OBSOLETE(UMatData.DEVICE_COPY_OBSOLETE), TEMP_UMAT(UMatData.TEMP_UMAT), TEMP_COPIED_UMAT(UMatData.TEMP_COPIED_UMAT), USER_ALLOCATED(UMatData.USER_ALLOCATED), DEVICE_MEM_MAPPED(UMatData.DEVICE_MEM_MAPPED);

    public final int value;

    UMatDataEnum(int value) {
        this.value = value;
    }
}
