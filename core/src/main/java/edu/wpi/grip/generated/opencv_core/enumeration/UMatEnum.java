package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum UMatEnum {

    MAGIC_VAL(opencv_core.UMat.MAGIC_VAL), AUTO_STEP(opencv_core.UMat.AUTO_STEP), CONTINUOUS_FLAG(opencv_core.UMat.CONTINUOUS_FLAG), SUBMATRIX_FLAG(opencv_core.UMat.SUBMATRIX_FLAG), MAGIC_MASK(opencv_core.UMat.MAGIC_MASK), TYPE_MASK(opencv_core.UMat.TYPE_MASK), DEPTH_MASK(opencv_core.UMat.DEPTH_MASK);

    public final int value;

    UMatEnum(int value) {
        this.value = value;
    }
}
