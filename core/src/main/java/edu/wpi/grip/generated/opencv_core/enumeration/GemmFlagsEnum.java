package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.global.opencv_core;

public enum GemmFlagsEnum {

    /** transposes src1 */
    GEMM_1_T(opencv_core.GEMM_1_T), /** transposes src2 */
    GEMM_2_T(opencv_core.GEMM_2_T), /** transposes src3 */
    GEMM_3_T(opencv_core.GEMM_3_T);

    public final int value;

    GemmFlagsEnum(int value) {
        this.value = value;
    }
}
