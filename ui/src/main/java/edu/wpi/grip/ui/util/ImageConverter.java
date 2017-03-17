package edu.wpi.grip.ui.util;

import com.google.common.primitives.UnsignedBytes;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_imgproc;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import static org.bytedeco.javacpp.opencv_core.CV_8S;
import static org.bytedeco.javacpp.opencv_core.CV_8U;

/**
 * Utility class for creating a JavaFX image from an OpenCV image.  This used by the preview views
 * to render an image in the GUI.
 */
public final class ImageConverter {
  private WritableImage image;
  private IntBuffer pixels;


  /**
   * Convert a BGR-formatted OpenCV {@link Mat} into a JavaFX {@link Image}. JavaFX understands ARGB
   * pixel data, so one way to turn a Mat into a JavaFX image is to shift around the bytes from the
   * Mat into an int array of pixels. This is also possible to do by using JavaCV, but the JavaCV
   * method involves several intermediate conversions (Mat -> Frame -> BufferedImage -> JavaFX
   * Image) and is way too slow to use for a real-time video.
   *
   * @param mat           An 8-bit OpenCV Mat containing an image with either 1 or 3 channels
   * @param desiredHeight the desired height of the image
   *
   * @return A JavaFX image, or null for empty
   */
  public Image convert(Mat mat, int desiredHeight) {
    /*
     * IMPORTANT!
     * The {@link ImageConverter#image} is a component that may be actively part of the UI
     * If we are changing it while it is being rendered by the UI thread this could cause
     * a problem in the UI thread.
     */
    if (!Platform.isFxApplicationThread()) {
      throw new IllegalStateException("This modifies an FX object. This must be run in the UI "
          + "Thread");
    }

    final int channels = mat.channels();

    assert channels == 3 || channels == 1 :
        "Only 3-channel BGR images or single-channel grayscale images can be converted";

    assert mat.depth() == CV_8U || mat.depth() == CV_8S :
        "Only images with 8 bits per channel can be previewed";

    // Don't try to render empty images.
    if (mat.empty()) {
      return null;
    }

    Mat toRender;
    if (mat.rows() > desiredHeight) {
      // Scale the image down
      toRender = new Mat();
      opencv_imgproc.resize(
          mat, toRender,
          new Size((int) (((double) mat.cols() * desiredHeight) / mat.rows()), desiredHeight),
          0, 0, opencv_imgproc.INTER_CUBIC
      );
    } else {
      toRender = mat;
    }

    final int width = toRender.cols();
    final int height = toRender.rows();

    // If the size of the Mat changed for whatever reason, allocate a new image with the proper
    // dimensions and a buffer big enough to hold all of the pixels in the image.
    if (this.image == null || this.image.getWidth() != width || this.image.getHeight() != height) {
      this.image = new WritableImage(width, height);
      this.pixels = IntBuffer.allocate(width * height);
    }

    final ByteBuffer buffer = toRender.createBuffer();
    final int stride = buffer.capacity() / height;

    // Convert the data from the Mat into ARGB data that we can put into a JavaFX WritableImage
    switch (channels) {
      case 1:
        // 1 channel - convert grayscale to ARGB
        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            final int value = UnsignedBytes.toInt(buffer.get(stride * y + channels * x));
            this.pixels.put(width * y + x, (0xff << 24) | (value << 16) | (value << 8) | value);
          }
        }

        break;

      case 3:
        // 3 channels - convert BGR to RGBA
        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            final int b = UnsignedBytes.toInt(buffer.get(stride * y + channels * x));
            final int g = UnsignedBytes.toInt(buffer.get(stride * y + channels * x + 1));
            final int r = UnsignedBytes.toInt(buffer.get(stride * y + channels * x + 2));
            this.pixels.put(width * y + x, (0xff << 24) | (r << 16) | (g << 8) | b);
          }
        }

        break;
      default:
        throw new UnsupportedOperationException("Only 1 or 3 channel images can be shown, tried "
            + "to show a " + channels + " channel image");
    }

    final PixelFormat<IntBuffer> argb = PixelFormat.getIntArgbInstance();
    this.image.getPixelWriter().setPixels(0, 0, width, height, argb, this.pixels, width);

    return this.image;
  }

  /**
   * Convert a BGR-formatted OpenCV {@link Mat} into a JavaFX {@link Image}. JavaFX understands ARGB
   * pixel data, so one way to turn a Mat into a JavaFX image is to shift around the bytes from the
   * Mat into an int array of pixels. This is also possible to do by using JavaCV, but the JavaCV
   * method involves several intermediate conversions (Mat -> Frame -> BufferedImage -> JavaFX
   * Image) and is way too slow to use for a real-time video.
   *
   * @param mat An 8-bit OpenCV Mat containing an image with either 1 or 3 channels
   *
   * @return A JavaFX image, or null for empty
   */
  Image convert(Mat mat) {
    return convert(mat, mat.rows());
  }

}
