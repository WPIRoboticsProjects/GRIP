package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.javacpp.opencv_imgproc;

public enum UndistortTypesEnum {

    PROJ_SPHERICAL_ORTHO(opencv_imgproc.PROJ_SPHERICAL_ORTHO), PROJ_SPHERICAL_EQRECT(opencv_imgproc.PROJ_SPHERICAL_EQRECT);

    public final int value;

    UndistortTypesEnum(int value) {
        this.value = value;
    }
}
