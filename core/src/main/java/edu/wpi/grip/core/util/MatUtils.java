package edu.wpi.grip.core.util;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.bitwise_xor;
import static org.bytedeco.javacpp.opencv_imgproc.CV_GRAY2BGR;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * Utility class for working with OpenCV Mats.
 */
public class MatUtils {

  /**
   * Draws some data on an image, such as lines, contours, or other shapes.
   *
   * @param image           the image
   * @param showOriginal    true if the original image should be shown
   * @param dataSupplier    supplier for the data being drawn on the image
   * @param drawingFunction the function to draw on the image with
   * @param <T>             the type of the data being drawn
   *
   * @return a mat with the data drawn on it
   */
  public static <T> Mat draw(Mat image,
                             boolean showOriginal,
                             Supplier<T> dataSupplier,
                             BiConsumer<Mat, T> drawingFunction) {
    Mat tmp = new Mat();
    if (image.channels() == 3) {
      image.copyTo(tmp);
    } else {
      cvtColor(image, tmp, CV_GRAY2BGR);
    }
    if (!showOriginal) {
      bitwise_xor(tmp, tmp, tmp);
    }

    drawingFunction.accept(tmp, dataSupplier.get());

    return tmp;
  }

}
