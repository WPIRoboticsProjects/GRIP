package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.opencv_core.TermCriteria;

public enum TermCriteriaTypeEnum {

    /** the maximum number of iterations or elements to compute */
    COUNT(TermCriteria.COUNT), MAX_ITER(TermCriteria.MAX_ITER), /** the desired accuracy or change in parameters at which the iterative algorithm stops */
    EPS(TermCriteria.EPS);

    public final int value;

    TermCriteriaTypeEnum(int value) {
        this.value = value;
    }
}
