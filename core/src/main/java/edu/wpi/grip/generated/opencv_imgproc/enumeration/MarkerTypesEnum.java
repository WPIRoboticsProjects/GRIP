package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.javacpp.opencv_imgproc;

public enum MarkerTypesEnum {

    /** A crosshair marker shape */
    MARKER_CROSS(opencv_imgproc.MARKER_CROSS), /** A 45 degree tilted crosshair marker shape */
    MARKER_TILTED_CROSS(opencv_imgproc.MARKER_TILTED_CROSS), /** A star marker shape, combination of cross and tilted cross */
    MARKER_STAR(opencv_imgproc.MARKER_STAR), /** A diamond marker shape */
    MARKER_DIAMOND(opencv_imgproc.MARKER_DIAMOND), /** A square marker shape */
    MARKER_SQUARE(opencv_imgproc.MARKER_SQUARE), /** An upwards pointing triangle marker shape */
    MARKER_TRIANGLE_UP(opencv_imgproc.MARKER_TRIANGLE_UP), /** A downwards pointing triangle marker shape */
    MARKER_TRIANGLE_DOWN(opencv_imgproc.MARKER_TRIANGLE_DOWN);

    public final int value;

    MarkerTypesEnum(int value) {
        this.value = value;
    }
}
