package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.operations.network.PublishValue;
import edu.wpi.grip.core.operations.network.Publishable;
import edu.wpi.grip.core.sockets.NoSocketTypeLabel;

import com.google.common.collect.ImmutableList;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the results of a rectangle detection operation.
 */
@NoSocketTypeLabel
public class RectsReport implements Publishable {

  private final Mat image;
  private final List<Rect> rectangles;

  public static final RectsReport NIL = new RectsReport(new Mat(), new ArrayList<>());

  public RectsReport(Mat image, List<Rect> rectangles) {
    this.image = image;
    this.rectangles = ImmutableList.copyOf(rectangles);
  }

  /**
   * Gets the image the rectangles are for.
   */
  public Mat getImage() {
    return image;
  }

  /**
   * Gets the rectangles in this report.
   */
  public List<Rect> getRectangles() {
    return rectangles;
  }

  /**
   * An array of the coordinates of the X-values of the top-left corner of every rectangle.
   */
  @PublishValue(key = "x", weight = 0)
  public double[] topLeftX() {
    return rectangles.stream()
        .mapToDouble(Rect::x)
        .toArray();
  }


  /**
   * An array of the coordinates of the Y-values of the top-left corner of every rectangle.
   */
  @PublishValue(key = "y", weight = 1)
  public double[] topLeftY() {
    return rectangles.stream()
        .mapToDouble(Rect::y)
        .toArray();
  }

  /**
   * An array of the widths of every rectangle.
   */
  @PublishValue(key = "width", weight = 2)
  public double[] width() {
    return rectangles.stream()
        .mapToDouble(Rect::width)
        .toArray();
  }

  /**
   * An array of the heights of every rectangle.
   */
  @PublishValue(key = "height", weight = 3)
  public double[] height() {
    return rectangles.stream()
        .mapToDouble(Rect::height)
        .toArray();
  }

}
