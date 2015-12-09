package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum ReduceTypesEnum {

    /** the output is the sum of all rows/columns of the matrix. */
    REDUCE_SUM(opencv_core.REDUCE_SUM), /** the output is the mean vector of all rows/columns of the matrix. */
    REDUCE_AVG(opencv_core.REDUCE_AVG), /** the output is the maximum (column/row-wise) of all rows/columns of the matrix. */
    REDUCE_MAX(opencv_core.REDUCE_MAX), /** the output is the minimum (column/row-wise) of all rows/columns of the matrix. */
    REDUCE_MIN(opencv_core.REDUCE_MIN);

    public final int value;

    ReduceTypesEnum(int value) {
        this.value = value;
    }
}
