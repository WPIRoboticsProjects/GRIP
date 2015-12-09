package edu.wpi.grip.core.util;


import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A utility wrapper for loading images from the file system.
 */
public final class ImageLoadingUtility {
    private ImageLoadingUtility() { /* no op */ }

    public static void loadImage(String path, Mat dst) throws IOException {
        loadImage(path, opencv_imgcodecs.IMREAD_COLOR, dst);
    }

    /**
     * Loads the image into the destination Mat
     *
     * @param path  The location on the file system where the image exists.
     * @param flags Flags to pass to imread {@link opencv_imgcodecs#imread(String, int)}
     * @param dst   The matrix to load the image into.
     */
    public static void loadImage(String path, final int flags, Mat dst) throws IOException {
        checkNotNull(path, "The path can not be null");
        checkNotNull(dst, "The destination Mat can not be null");
        final Mat img = opencv_imgcodecs.imread(path, flags);
        if (img != null && !img.empty() && !img.isNull()) {
            img.copyTo(dst);
        } else {
            throw new IOException("Error loading image " + path);
        }
    }

}
