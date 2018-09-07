package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum cudaFeatureSetEnum {

    FEATURE_SET_COMPUTE_10(opencv_core.FEATURE_SET_COMPUTE_10), FEATURE_SET_COMPUTE_11(opencv_core.FEATURE_SET_COMPUTE_11), FEATURE_SET_COMPUTE_12(opencv_core.FEATURE_SET_COMPUTE_12), FEATURE_SET_COMPUTE_13(opencv_core.FEATURE_SET_COMPUTE_13), FEATURE_SET_COMPUTE_20(opencv_core.FEATURE_SET_COMPUTE_20), FEATURE_SET_COMPUTE_21(opencv_core.FEATURE_SET_COMPUTE_21), FEATURE_SET_COMPUTE_30(opencv_core.FEATURE_SET_COMPUTE_30), FEATURE_SET_COMPUTE_32(opencv_core.FEATURE_SET_COMPUTE_32), FEATURE_SET_COMPUTE_35(opencv_core.FEATURE_SET_COMPUTE_35), FEATURE_SET_COMPUTE_50(opencv_core.FEATURE_SET_COMPUTE_50), GLOBAL_ATOMICS(opencv_core.GLOBAL_ATOMICS), SHARED_ATOMICS(opencv_core.SHARED_ATOMICS), NATIVE_DOUBLE(opencv_core.NATIVE_DOUBLE), WARP_SHUFFLE_FUNCTIONS(opencv_core.WARP_SHUFFLE_FUNCTIONS), DYNAMIC_PARALLELISM(opencv_core.DYNAMIC_PARALLELISM);

    public final int value;

    cudaFeatureSetEnum(int value) {
        this.value = value;
    }
}
