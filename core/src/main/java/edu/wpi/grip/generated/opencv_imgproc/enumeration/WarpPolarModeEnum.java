package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.javacpp.opencv_imgproc;

public enum WarpPolarModeEnum {

    /** Remaps an image to/from polar space. */
    WARP_POLAR_LINEAR(opencv_imgproc.WARP_POLAR_LINEAR), /** Remaps an image to/from semilog-polar space. */
    WARP_POLAR_LOG(opencv_imgproc.WARP_POLAR_LOG);

    public final int value;

    WarpPolarModeEnum(int value) {
        this.value = value;
    }
}
