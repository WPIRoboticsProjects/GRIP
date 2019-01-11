package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum MatEnum {

    MAGIC_VAL(opencv_core.Mat.MAGIC_VAL), AUTO_STEP(opencv_core.Mat.AUTO_STEP), CONTINUOUS_FLAG(opencv_core.Mat.CONTINUOUS_FLAG), SUBMATRIX_FLAG(opencv_core.Mat.SUBMATRIX_FLAG), MAGIC_MASK(opencv_core.Mat.MAGIC_MASK), TYPE_MASK(opencv_core.Mat.TYPE_MASK), DEPTH_MASK(opencv_core.Mat.DEPTH_MASK);

    public final int value;

    MatEnum(int value) {
        this.value = value;
    }
}
