package edu.wpi.grip.core.util;

import org.bytedeco.javacpp.opencv_core;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;

/**
 * Shims for working with OpenCV and JavaCV wrappers.
 */
public final class OpenCvShims {

  private OpenCvShims() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Copies the data of a JavaCV Mat to an OpenCV mat.
   */
  public static void copyJavaCvMatToOpenCvMat(opencv_core.Mat src, Mat dst) {
    final int width = src.cols();
    final int height = src.rows();
    final int channels = src.channels();

    final ByteBuffer buffer = src.createBuffer();
    byte[] bytes = new byte[width * height * channels];
    buffer.get(bytes);

    // Store the data in a temporary mat to get the type, size, etc. correct, since Mat doesn't
    // expose methods for directly changing its size or type
    final Mat tmp = new Mat(src.rows(), src.cols(), src.type());
    tmp.put(0, 0, bytes);
    tmp.copyTo(dst);
    tmp.release();
  }

}
