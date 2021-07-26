package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.opencv_core.SparseMat;

public enum SparseMatEnum {

  MAGIC_VAL(SparseMat.MAGIC_VAL), MAX_DIM(SparseMat.MAX_DIM), HASH_SCALE(SparseMat.HASH_SCALE), HASH_BIT(SparseMat.HASH_BIT);

  public final int value;

  SparseMatEnum(int value) {
    this.value = value;
  }
}
