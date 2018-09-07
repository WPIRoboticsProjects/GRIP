package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.javacpp.opencv_imgproc;

public enum ConnectedComponentsAlgorithmsTypesEnum {

    /** SAUF algorithm for 8-way connectivity, SAUF algorithm for 4-way connectivity */
    CCL_WU(opencv_imgproc.CCL_WU), /** BBDT algorithm for 8-way connectivity, SAUF algorithm for 4-way connectivity */
    CCL_DEFAULT(opencv_imgproc.CCL_DEFAULT), /** BBDT algorithm for 8-way connectivity, SAUF algorithm for 4-way connectivity */
    CCL_GRANA(opencv_imgproc.CCL_GRANA);

    public final int value;

    ConnectedComponentsAlgorithmsTypesEnum(int value) {
        this.value = value;
    }
}
