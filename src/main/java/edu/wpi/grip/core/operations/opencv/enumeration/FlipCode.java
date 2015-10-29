package edu.wpi.grip.core.operations.opencv.enumeration;

/**
 * FlipCode
 * Codes from documentation in {@link org.bytedeco.javacpp.opencv_core#flip}
 */
public enum FlipCode {
    // IMPORTANT! If you change the name of these values then you must also change the FileParser in the generator.
    X_AXIS(0),
    Y_AXIS(1),
    BOTH_AXES(-1);

    public final int value;

    FlipCode(int value) {
        this.value = value;
    }
}
