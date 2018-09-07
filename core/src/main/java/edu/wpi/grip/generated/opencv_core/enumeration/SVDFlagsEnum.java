package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum SVDFlagsEnum {

    /** allow the algorithm to modify the decomposed matrix; it can save space and speed up
            processing. currently ignored. */
    MODIFY_A(opencv_core.SVD.MODIFY_A), /** indicates that only a vector of singular values {@code w} is to be processed, while u and vt
            will be set to empty matrices */
    NO_UV(opencv_core.SVD.NO_UV), /** when the matrix is not square, by default the algorithm produces u and vt matrices of
            sufficiently large size for the further A reconstruction; if, however, FULL_UV flag is
            specified, u and vt will be full-size square orthogonal matrices.*/
    FULL_UV(opencv_core.SVD.FULL_UV);

    public final int value;

    SVDFlagsEnum(int value) {
        this.value = value;
    }
}
