package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum cudaDeviceInfoEnum {

    /** default compute mode (Multiple threads can use cudaSetDevice with this device) */
    ComputeModeDefault(opencv_core.DeviceInfo.ComputeModeDefault), /** compute-exclusive-thread mode (Only one thread in one process will be able to use cudaSetDevice with this device) */
    ComputeModeExclusive(opencv_core.DeviceInfo.ComputeModeExclusive), /** compute-prohibited mode (No threads can use cudaSetDevice with this device) */
    ComputeModeProhibited(opencv_core.DeviceInfo.ComputeModeProhibited), /** compute-exclusive-process mode (Many threads in one process will be able to use cudaSetDevice with this device) */
    ComputeModeExclusiveProcess(opencv_core.DeviceInfo.ComputeModeExclusiveProcess);

    public final int value;

    cudaDeviceInfoEnum(int value) {
        this.value = value;
    }
}
