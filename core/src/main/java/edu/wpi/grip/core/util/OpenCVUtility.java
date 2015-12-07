package edu.wpi.grip.core.util;


import com.google.common.eventbus.EventBus;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpenCVUtility {
    private OpenCVUtility() { /* no op */ }

    public static void loadImage(String path, Mat dst) throws IOException {
        loadImage(path, opencv_imgcodecs.IMREAD_COLOR, dst);
    }

    /**
     * Loads the image and posts an update to the {@link EventBus}
     *
     * @param path  The location on the file system where the image exists.
     * @param flags Flags to pass to imread {@link opencv_imgcodecs#imread(String, int)}
     */
    public static void loadImage(String path, final int flags, Mat dst) throws IOException {
        checkNotNull(path, "The path can not be null");
        checkNotNull(dst, "The destination Mat can not be null");
        final Mat img = opencv_imgcodecs.imread(path, flags);
        if (!img.empty()) {
            img.copyTo(dst);
        } else {
            throw new IOException("Error loading image " + path);
        }
    }

}
