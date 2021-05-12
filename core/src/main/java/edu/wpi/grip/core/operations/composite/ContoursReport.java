package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.PublishableObject;
import edu.wpi.grip.core.operations.network.PublishValue;
import edu.wpi.grip.core.operations.network.Publishable;
import edu.wpi.grip.core.sockets.NoSocketTypeLabel;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.util.LazyInit;
import edu.wpi.grip.core.util.PointerStream;

import com.google.auto.value.AutoValue;

import org.bytedeco.javacpp.opencv_core.RotatedRect;
import org.bytedeco.javacpp.opencv_imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_imgproc.contourArea;
import static org.bytedeco.javacpp.opencv_imgproc.convexHull;

/**
 * The output of {@link FindContoursOperation}.  This stores a list of contours (which is basically
 * a list of points) in OpenCV objects, as well as the width and height of the image that the
 * contours are from, to give context to the points.
 */
@PublishableObject
@NoSocketTypeLabel
public final class ContoursReport implements Publishable {

  private final int rows;
  private final int cols;
  private final MatVector contours;
  private final LazyInit<Rect[]> boundingBoxes = new LazyInit<>(this::computeBoundingBoxes);
  private final LazyInit<RotatedRect[]> rotatedBoundingBoxes =
      new LazyInit<>(this::computeMinAreaBoundingBoxes);

  /**
   * Construct an empty report.  This is used as a default value for {@link Socket}s containing
   * ContoursReports.
   */
  public ContoursReport() {
    this(new MatVector(), 0, 0);
  }

  ContoursReport(MatVector contours, int rows, int cols) {
    this.contours = contours;
    this.rows = rows;
    this.cols = cols;
  }

  public int getRows() {
    return this.rows;
  }

  public int getCols() {
    return this.cols;
  }

  public MatVector getContours() {
    return this.contours;
  }

  /**
   * @return All of the contours held within this report.
   */
  public List<Contour> getProcessedContours() {
    final List<Contour> processedContours = new ArrayList<>((int) contours.size());
    double[] area = getArea();
    double[] centerX = getCenterX();
    double[] centerY = getCenterY();
    double[] width = getWidth();
    double[] height = getHeights();
    double[] solidity = getSolidity();
    double[] angles = getAngles();
    for (int i = 0; i < contours.size(); i++) {
      processedContours.add(Contour.create(area[i], centerX[i], centerY[i], width[i], height[i],
          solidity[i], angles[i]));
    }
    return processedContours;
  }

  /**
   * Compute the bounding boxes of all contours. Called lazily and cached by {@link #boundingBoxes}.
   */
  private Rect[] computeBoundingBoxes() {
    return PointerStream.ofMatVector(contours)
        .map(opencv_imgproc::boundingRect)
        .toArray(Rect[]::new);
  }

  /**
   * Computes the minimum-area bounding boxes of all contours. Called lazily and cached by
   * {@link #rotatedBoundingBoxes}.
   */
  private RotatedRect[] computeMinAreaBoundingBoxes() {
    return PointerStream.ofMatVector(contours)
        .map(opencv_imgproc::minAreaRect)
        .toArray(RotatedRect[]::new);
  }

  @PublishValue(key = "area", weight = 0)
  public double[] getArea() {
    return PointerStream.ofMatVector(contours)
        .mapToDouble(opencv_imgproc::contourArea)
        .toArray();
  }

  @PublishValue(key = "centerX", weight = 1)
  public double[] getCenterX() {
    return Stream.of(boundingBoxes.get())
        .mapToDouble(r -> r.x() + r.width() / 2)
        .toArray();
  }

  @PublishValue(key = "centerY", weight = 2)
  public double[] getCenterY() {
    return Stream.of(boundingBoxes.get())
        .mapToDouble(r -> r.y() + r.height() / 2)
        .toArray();
  }

  @PublishValue(key = "width", weight = 3)
  public synchronized double[] getWidth() {
    return Stream.of(boundingBoxes.get())
        .mapToDouble(Rect::width)
        .toArray();
  }

  @PublishValue(key = "height", weight = 4)
  public synchronized double[] getHeights() {
    return Stream.of(boundingBoxes.get())
        .mapToDouble(Rect::height)
        .toArray();
  }

  @PublishValue(key = "solidity", weight = 5)
  public synchronized double[] getSolidity() {
    final double[] solidities = new double[(int) contours.size()];
    Mat hull = new Mat();
    for (int i = 0; i < contours.size(); i++) {
      convexHull(contours.get(i), hull);
      solidities[i] = contourArea(contours.get(i)) / contourArea(hull);
    }
    hull.release();
    return solidities;
  }

  @PublishValue(key = "angle", weight = 6)
  public synchronized double[] getAngles() {
    return Stream.of(rotatedBoundingBoxes.get())
        .mapToDouble(RotatedRect::angle)
        .toArray();
  }

  @AutoValue
  public abstract static class Contour {
    public static Contour create(double area, double centerX, double centerY, double width, double
        height, double solidity, double angle) {
      return new AutoValue_ContoursReport_Contour(area, centerX, centerY, width, height, solidity,
          angle);
    }

    public abstract double area();

    public abstract double centerX();

    public abstract double centerY();

    public abstract double width();

    public abstract double height();

    public abstract double solidity();

    public abstract double angle();
  }
}
