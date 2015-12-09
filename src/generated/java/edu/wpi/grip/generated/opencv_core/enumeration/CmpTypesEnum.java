package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum CmpTypesEnum {

    /** src1 is equal to src2. */
    CMP_EQ(opencv_core.CMP_EQ), /** src1 is greater than src2. */
    CMP_GT(opencv_core.CMP_GT), /** src1 is greater than or equal to src2. */
    CMP_GE(opencv_core.CMP_GE), /** src1 is less than src2. */
    CMP_LT(opencv_core.CMP_LT), /** src1 is less than or equal to src2. */
    CMP_LE(opencv_core.CMP_LE), /** src1 is unequal to src2. */
    CMP_NE(opencv_core.CMP_NE);

    public final int value;

    CmpTypesEnum(int value) {
        this.value = value;
    }
}
