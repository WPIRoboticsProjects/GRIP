package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.operations.network.PublishValue;
import edu.wpi.grip.core.operations.network.Publishable;
import edu.wpi.grip.core.sockets.NoSocketTypeLabel;
import edu.wpi.grip.core.sockets.Socket;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_imgproc.boundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.contourArea;
import static org.bytedeco.javacpp.opencv_imgproc.convexHull;

/**
 * The output of {@link FindContoursOperation}.  This stores a list of contours (which is basically
 * a list of points) in OpenCV objects, as well as the width and height of the image that the
 * contours are from, to give context to the points.
 */
@NoSocketTypeLabel
public final class ContoursReport implements Publishable {

  private final int rows;
  private final int cols;
  private final MatVector contours;
  private Optional<Rect[]> boundingBoxes = Optional.empty();

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
    for (int i = 0; i < contours.size(); i++) {
      processedContours.add(Contour.create(area[i], centerX[i], centerY[i], width[i], height[i],
          solidity[i]));
    }
    return processedContours;
  }

  /**
   * Compute the bounding boxes of all contours (if they haven't already been computed).  Bounding
   * boxes are used to compute several different properties, so it's probably not a good idea to
   * compute them over and over again.
   */
  private synchronized Rect[] computeBoundingBoxes() {
    if (!boundingBoxes.isPresent()) {
      Rect[] bb = new Rect[(int) contours.size()];
      for (int i = 0; i < contours.size(); i++) {
        bb[i] = boundingRect(contours.get(i));
      }

      boundingBoxes = Optional.of(bb);
    }

    return boundingBoxes.get();
  }

  @PublishValue(key = "area", weight = 0)
  public double[] getArea() {
    final double[] areas = new double[(int) contours.size()];
    for (int i = 0; i < contours.size(); i++) {
      areas[i] = contourArea(contours.get(i));
    }
    return areas;
  }

  @PublishValue(key = "centerX", weight = 1)
  public double[] getCenterX() {
    final double[] centers = new double[(int) contours.size()];
    final Rect[] boundingBoxes = computeBoundingBoxes();
    for (int i = 0; i < contours.size(); i++) {
      centers[i] = boundingBoxes[i].x() + boundingBoxes[i].width() / 2;
    }
    return centers;
  }

  @PublishValue(key = "centerY", weight = 2)
  public double[] getCenterY() {
    final double[] centers = new double[(int) contours.size()];
    final Rect[] boundingBoxes = computeBoundingBoxes();
    for (int i = 0; i < contours.size(); i++) {
      centers[i] = boundingBoxes[i].y() + boundingBoxes[i].height() / 2;
    }
    return centers;
  }

  @PublishValue(key = "width", weight = 3)
  public synchronized double[] getWidth() {
    final double[] widths = new double[(int) contours.size()];
    final Rect[] boundingBoxes = computeBoundingBoxes();
    for (int i = 0; i < contours.size(); i++) {
      widths[i] = boundingBoxes[i].width();
    }
    return widths;
  }

  @PublishValue(key = "height", weight = 4)
  public synchronized double[] getHeights() {
    final double[] heights = new double[(int) contours.size()];
    final Rect[] boundingBoxes = computeBoundingBoxes();
    for (int i = 0; i < contours.size(); i++) {
      heights[i] = boundingBoxes[i].height();
    }
    return heights;
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

  @AutoValue
  public abstract static class Contour {
    static Contour create(double area, double centerX, double centerY, double width, double
        height, double solidity) {
      return new AutoValue_ContoursReport_Contour(area, centerX, centerY, width, height, solidity);
    }

    public abstract double area();

    public abstract double centerX();

    public abstract double centerY();

    public abstract double width();

    public abstract double height();

    public abstract double solidity();
  }
}
