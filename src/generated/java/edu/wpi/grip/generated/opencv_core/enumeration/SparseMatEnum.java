package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum SparseMatEnum {

    MAGIC_VAL(opencv_core.SparseMat.MAGIC_VAL), MAX_DIM(opencv_core.SparseMat.MAX_DIM), HASH_SCALE(opencv_core.SparseMat.HASH_SCALE), HASH_BIT(opencv_core.SparseMat.HASH_BIT);

    public final int value;

    SparseMatEnum(int value) {
        this.value = value;
    }
}
