package edu.wpi.grip.core.util;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wraps common opencv functions that are inherently unsafe to make them throw exceptions instead of segfaulting.
 */
public final class OpenCVSafe {

    private OpenCVSafe () {
        /* no-op */
    }

    /**
     * @see opencv_core#inRange(Mat, Mat, Mat, Mat)
     */
    public static void inRange(Mat src, Mat lowerb, Mat upperb, Mat dst) throws NullPointerException, IllegalStateException {
        checkArgument(src.channels() == 3, "Input must be a three channel input");
        checkMat(src, "src");
        checkMat(lowerb, "lowerb");
        checkMat(upperb, "upperb");
        checkNotNull(dst, "dst must not be null");
        opencv_core.inRange(src, lowerb, upperb, dst);
    }

    private static void checkMat(Mat mat, String name) {
        checkNotNull(mat, name + " can not be null");
        checkArgument(!mat.empty(), name + " can not be empty");
        checkArgument(!mat.isNull(), name + " can not be null reference");
    }
}
