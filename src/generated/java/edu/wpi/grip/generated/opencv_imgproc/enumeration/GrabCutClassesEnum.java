package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.javacpp.opencv_imgproc;

public enum GrabCutClassesEnum {

    /** an obvious background pixels */
    GC_BGD(opencv_imgproc.GC_BGD), /** an obvious foreground (object) pixel */
    GC_FGD(opencv_imgproc.GC_FGD), /** a possible background pixel */
    GC_PR_BGD(opencv_imgproc.GC_PR_BGD), /** a possible foreground pixel */
    GC_PR_FGD(opencv_imgproc.GC_PR_FGD);

    public final int value;

    GrabCutClassesEnum(int value) {
        this.value = value;
    }
}
