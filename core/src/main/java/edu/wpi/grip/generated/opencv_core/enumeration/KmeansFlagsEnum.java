package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.global.opencv_core;

public enum KmeansFlagsEnum {

    /** Select random initial centers in each attempt.*/
    KMEANS_RANDOM_CENTERS(opencv_core.KMEANS_RANDOM_CENTERS), /** Use kmeans++ center initialization by Arthur and Vassilvitskii [Arthur2007].*/
    KMEANS_PP_CENTERS(opencv_core.KMEANS_PP_CENTERS), /** During the first (and possibly the only) attempt, use the
        user-supplied labels instead of computing them from the initial centers. For the second and
        further attempts, use the random or semi-random centers. Use one of KMEANS_\*_CENTERS flag
        to specify the exact method.*/
    KMEANS_USE_INITIAL_LABELS(opencv_core.KMEANS_USE_INITIAL_LABELS);

    public final int value;

    KmeansFlagsEnum(int value) {
        this.value = value;
    }
}
