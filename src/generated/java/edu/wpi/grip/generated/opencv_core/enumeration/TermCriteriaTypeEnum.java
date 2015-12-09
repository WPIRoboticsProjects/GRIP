package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum TermCriteriaTypeEnum {

    /** the maximum number of iterations or elements to compute */
    COUNT(opencv_core.TermCriteria.COUNT), MAX_ITER(opencv_core.TermCriteria.MAX_ITER), /** the desired accuracy or change in parameters at which the iterative algorithm stops */
    EPS(opencv_core.TermCriteria.EPS);

    public final int value;

    TermCriteriaTypeEnum(int value) {
        this.value = value;
    }
}
