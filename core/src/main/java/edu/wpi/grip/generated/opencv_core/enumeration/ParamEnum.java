package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum ParamEnum {

    INT(opencv_core.Param.INT), BOOLEAN(opencv_core.Param.BOOLEAN), REAL(opencv_core.Param.REAL), STRING(opencv_core.Param.STRING), MAT(opencv_core.Param.MAT), MAT_VECTOR(opencv_core.Param.MAT_VECTOR), ALGORITHM(opencv_core.Param.ALGORITHM), FLOAT(opencv_core.Param.FLOAT), UNSIGNED_INT(opencv_core.Param.UNSIGNED_INT), UINT64(opencv_core.Param.UINT64), UCHAR(opencv_core.Param.UCHAR);

    public final int value;

    ParamEnum(int value) {
        this.value = value;
    }
}
