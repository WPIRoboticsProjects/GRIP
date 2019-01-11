package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum SolveLPResultEnum {

    /** problem is unbounded (target function can achieve arbitrary high values) */
    SOLVELP_UNBOUNDED(opencv_core.SOLVELP_UNBOUNDED), /** problem is unfeasible (there are no points that satisfy all the constraints imposed) */
    SOLVELP_UNFEASIBLE(opencv_core.SOLVELP_UNFEASIBLE), /** there is only one maximum for target function */
    SOLVELP_SINGLE(opencv_core.SOLVELP_SINGLE), /** there are multiple maxima for target function - the arbitrary one is returned */
    SOLVELP_MULTI(opencv_core.SOLVELP_MULTI);

    public final int value;

    SolveLPResultEnum(int value) {
        this.value = value;
    }
}
