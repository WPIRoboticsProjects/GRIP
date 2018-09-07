package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum oclDeviceEnum {

    TYPE_DEFAULT(opencv_core.Device.TYPE_DEFAULT), TYPE_CPU(opencv_core.Device.TYPE_CPU), TYPE_GPU(opencv_core.Device.TYPE_GPU), TYPE_ACCELERATOR(opencv_core.Device.TYPE_ACCELERATOR), TYPE_DGPU(opencv_core.Device.TYPE_DGPU), TYPE_IGPU(opencv_core.Device.TYPE_IGPU), TYPE_ALL(opencv_core.Device.TYPE_ALL), FP_DENORM(opencv_core.Device.FP_DENORM), FP_INF_NAN(opencv_core.Device.FP_INF_NAN), FP_ROUND_TO_NEAREST(opencv_core.Device.FP_ROUND_TO_NEAREST), FP_ROUND_TO_ZERO(opencv_core.Device.FP_ROUND_TO_ZERO), FP_ROUND_TO_INF(opencv_core.Device.FP_ROUND_TO_INF), FP_FMA(opencv_core.Device.FP_FMA), FP_SOFT_FLOAT(opencv_core.Device.FP_SOFT_FLOAT), FP_CORRECTLY_ROUNDED_DIVIDE_SQRT(opencv_core.Device.FP_CORRECTLY_ROUNDED_DIVIDE_SQRT), EXEC_KERNEL(opencv_core.Device.EXEC_KERNEL), EXEC_NATIVE_KERNEL(opencv_core.Device.EXEC_NATIVE_KERNEL), NO_CACHE(opencv_core.Device.NO_CACHE), READ_ONLY_CACHE(opencv_core.Device.READ_ONLY_CACHE), READ_WRITE_CACHE(opencv_core.Device.READ_WRITE_CACHE), NO_LOCAL_MEM(opencv_core.Device.NO_LOCAL_MEM), LOCAL_IS_LOCAL(opencv_core.Device.LOCAL_IS_LOCAL), LOCAL_IS_GLOBAL(opencv_core.Device.LOCAL_IS_GLOBAL), UNKNOWN_VENDOR(opencv_core.Device.UNKNOWN_VENDOR), VENDOR_AMD(opencv_core.Device.VENDOR_AMD), VENDOR_INTEL(opencv_core.Device.VENDOR_INTEL), VENDOR_NVIDIA(opencv_core.Device.VENDOR_NVIDIA);

    public final int value;

    oclDeviceEnum(int value) {
        this.value = value;
    }
}
