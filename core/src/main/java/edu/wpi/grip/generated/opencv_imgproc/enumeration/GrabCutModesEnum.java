package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.javacpp.opencv_imgproc;

public enum GrabCutModesEnum {

    /** The function initializes the state and the mask using the provided rectangle. After that it
    runs iterCount iterations of the algorithm. */
    GC_INIT_WITH_RECT(opencv_imgproc.GC_INIT_WITH_RECT), /** The function initializes the state using the provided mask. Note that GC_INIT_WITH_RECT
    and GC_INIT_WITH_MASK can be combined. Then, all the pixels outside of the ROI are
    automatically initialized with GC_BGD .*/
    GC_INIT_WITH_MASK(opencv_imgproc.GC_INIT_WITH_MASK), /** The value means that the algorithm should just resume. */
    GC_EVAL(opencv_imgproc.GC_EVAL);

    public final int value;

    GrabCutModesEnum(int value) {
        this.value = value;
    }
}
