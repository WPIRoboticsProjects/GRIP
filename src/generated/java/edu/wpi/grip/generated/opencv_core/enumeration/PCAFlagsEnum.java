package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum PCAFlagsEnum {

    /** indicates that the input samples are stored as matrix rows */
    DATA_AS_ROW(opencv_core.PCA.DATA_AS_ROW), /** indicates that the input samples are stored as matrix columns */
    DATA_AS_COL(opencv_core.PCA.DATA_AS_COL), USE_AVG(opencv_core.PCA.USE_AVG);

    public final int value;

    PCAFlagsEnum(int value) {
        this.value = value;
    }
}
