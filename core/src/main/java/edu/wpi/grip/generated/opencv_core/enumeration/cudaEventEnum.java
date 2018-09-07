package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum cudaEventEnum {

    /** Default event flag */
    DEFAULT(opencv_core.Event.DEFAULT), /** Event uses blocking synchronization */
    BLOCKING_SYNC(opencv_core.Event.BLOCKING_SYNC), /** Event will not record timing data */
    DISABLE_TIMING(opencv_core.Event.DISABLE_TIMING), /** Event is suitable for interprocess use. DisableTiming must be set */
    INTERPROCESS(opencv_core.Event.INTERPROCESS);

    public final int value;

    cudaEventEnum(int value) {
        this.value = value;
    }
}
