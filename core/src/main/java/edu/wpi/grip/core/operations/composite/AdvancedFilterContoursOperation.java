package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.Point2f;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_core.RotatedRect;
import static org.bytedeco.javacpp.opencv_imgproc.arcLength;
import static org.bytedeco.javacpp.opencv_imgproc.boundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.contourArea;
import static org.bytedeco.javacpp.opencv_imgproc.convexHull;
import static org.bytedeco.javacpp.opencv_imgproc.minAreaRect;

/**
 * An {@link Operation} that takes in a list of contours and outputs a list of any contours in the
 * input that match all of several criteria.  Right now, the user can specify a minimum area,
 * minimum perimeter, and ranges for width and height. This is useful because running a Find
 * Contours on a real-life image typically leads to many small undesirable contours from noise and
 * small objects, as well as contours that do not meet the expected characteristics of the feature
 * we're actually looking for.  So, this operation can help narrow them down.
 */
public class AdvancedFilterContoursOperation implements Operation {

  public static final OperationDescription DESCRIPTION =
      OperationDescription.builder()
          .name("Advanced Filter Contours")
          .summary("Find contours matching certain criteria")
          .category(OperationDescription.Category.FEATURE_DETECTION)
          .icon(Icon.iconStream("find-contours"))
          .build();

  private final SocketHint<ContoursReport> contoursHint = new SocketHint.Builder<>(ContoursReport
      .class)
      .identifier("Contours").initialValueSupplier(ContoursReport::new).build();

  private final SocketHint<Number> minAreaHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min Area", 0, 0, Integer.MAX_VALUE);

  private final SocketHint<Number> maxAreaHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Max Area", 10000, 0, Integer.MAX_VALUE);

  private final SocketHint<Number> minPerimeterHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min Perimeter", 0, 0, Integer.MAX_VALUE);

  private final SocketHint<Number> maxPerimeterHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Max Perimeter", 10000, 0,
          Integer.MAX_VALUE);

  private final SocketHint<Boolean> rotatedRectHint =
      SocketHints.createBooleanSocketHint("Rotated Rectangles", false);

  private final SocketHint<Number> minWidthHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min Width", 0, 0, Integer.MAX_VALUE);

  private final SocketHint<Number> maxWidthHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Max Width", 1000, 0, Integer.MAX_VALUE);

  private final SocketHint<Number> minHeightHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min Height", 0, 0, Integer.MAX_VALUE);

  private final SocketHint<Number> maxHeightHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Max Height", 1000, 0, Integer.MAX_VALUE);

  private final SocketHint<List<Number>> solidityHint =
      SocketHints.Inputs.createNumberListRangeSocketHint("Solidity", 0, 100);

  private final SocketHint<Number> minVertexHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min Vertices", 0, 0, Integer.MAX_VALUE);

  private final SocketHint<Number> maxVertexHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Max Vertices", 1000000, 0, Integer
          .MAX_VALUE);

  private final SocketHint<Number> minRatioHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min Ratio", 0, 0, Integer.MAX_VALUE);

  private final SocketHint<Number> maxRatioHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Max Ratio", 1000, 0, Integer.MAX_VALUE);


  private final InputSocket<ContoursReport> contoursSocket;
  private final InputSocket<Number> minAreaSocket;
  private final InputSocket<Number> maxAreaSocket;
  private final InputSocket<Number> minPerimeterSocket;
  private final InputSocket<Number> maxPerimeterSocket;
  private final InputSocket<Boolean> rotatedRectSocket;
  private final InputSocket<Number> minWidthSocket;
  private final InputSocket<Number> maxWidthSocket;
  private final InputSocket<Number> minHeightSocket;
  private final InputSocket<Number> maxHeightSocket;
  private final InputSocket<Number> minVertexSocket;
  private final InputSocket<Number> maxVertexSocket;
  private final InputSocket<Number> minRatioSocket;
  private final InputSocket<Number> maxRatioSocket;
  private final InputSocket<List<Number>> soliditySocket;

  private final OutputSocket<ContoursReport> outputSocket;

  @SuppressWarnings("JavadocMethod")
  public AdvancedFilterContoursOperation(InputSocket.Factory inputSocketFactory,
                                         OutputSocket.Factory outputSocketFactory) {
    this.contoursSocket = inputSocketFactory.create(contoursHint);
    this.minAreaSocket = inputSocketFactory.create(minAreaHint);
    this.maxAreaSocket = inputSocketFactory.create(maxAreaHint);
    this.minPerimeterSocket = inputSocketFactory.create(minPerimeterHint);
    this.maxPerimeterSocket = inputSocketFactory.create(maxPerimeterHint);
    this.rotatedRectSocket = inputSocketFactory.create(rotatedRectHint);
    this.minWidthSocket = inputSocketFactory.create(minWidthHint);
    this.maxWidthSocket = inputSocketFactory.create(maxWidthHint);
    this.minHeightSocket = inputSocketFactory.create(minHeightHint);
    this.maxHeightSocket = inputSocketFactory.create(maxHeightHint);
    this.minVertexSocket = inputSocketFactory.create(minVertexHint);
    this.maxVertexSocket = inputSocketFactory.create(maxVertexHint);
    this.minRatioSocket = inputSocketFactory.create(minRatioHint);
    this.maxRatioSocket = inputSocketFactory.create(maxRatioHint);
    this.soliditySocket = inputSocketFactory.create(solidityHint);

    this.outputSocket = outputSocketFactory.create(contoursHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        contoursSocket,
        minAreaSocket,
        maxAreaSocket,
        minPerimeterSocket,
        maxPerimeterSocket,
        rotatedRectSocket,
        minWidthSocket,
        maxWidthSocket,
        minHeightSocket,
        maxHeightSocket,
        minVertexSocket,
        maxVertexSocket,
        minRatioSocket,
        maxRatioSocket,
        soliditySocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        outputSocket
    );
  }

  @Override
  @SuppressWarnings("unchecked")
  public void perform() {
    final InputSocket<ContoursReport> inputSocket = contoursSocket;
    final double minArea = minAreaSocket.getValue().get().doubleValue();
    final double maxArea = maxAreaSocket.getValue().get().doubleValue();
    final double minPerimeter = minPerimeterSocket.getValue().get().doubleValue();
    final double maxPerimeter = maxPerimeterSocket.getValue().get().doubleValue();
    final boolean rotatedRect = rotatedRectSocket.getValue().get().booleanValue();
    final double minWidth = minWidthSocket.getValue().get().doubleValue();
    final double maxWidth = maxWidthSocket.getValue().get().doubleValue();
    final double minHeight = minHeightSocket.getValue().get().doubleValue();
    final double maxHeight = maxHeightSocket.getValue().get().doubleValue();
    final double minSolidity = soliditySocket.getValue().get().get(0).doubleValue();
    final double maxSolidity = soliditySocket.getValue().get().get(1).doubleValue();
    final double minVertexCount = minVertexSocket.getValue().get().doubleValue();
    final double maxVertexCount = maxVertexSocket.getValue().get().doubleValue();
    final double minRatio = minRatioSocket.getValue().get().doubleValue();
    final double maxRatio = maxRatioSocket.getValue().get().doubleValue();


    final MatVector inputContours = inputSocket.getValue().get().getContours();
    final MatVector outputContours = new MatVector(inputContours.size());
    final Mat hull = new Mat();

    // Add contours from the input vector to the output vector only if they pass all of the
    // criteria (minimum
    // area, minimum perimeter, width, and height, etc...)
    int filteredContourCount = 0;
    for (int i = 0; i < inputContours.size(); i++) {
      final Mat contour = inputContours.get(i);

      double width;
      double height;
      if (rotatedRect) {
        final RotatedRect bb = minAreaRect(contour);
        Point2f points = new Point2f(4);
        bb.points(points);
        final double rotatedWidth = Math.sqrt(Math.pow(points.position(0).x()
            - points.position(1).x(), 2)
            + Math.pow(points.position(0).y() - points.position(1).y(), 2));
        final double rotatedHeight = Math.sqrt( Math.pow(points.position(1).x()
            - points.position(2).x(), 2)
            + Math.pow(points.position(1).y() - points.position(2).y(), 2));
        if (Math.abs(bb.angle()) >= 45) {
          width = rotatedWidth;
          height = rotatedHeight;
        } else {
          width = rotatedHeight;
          height = rotatedWidth;
        }
      } else {
        final Rect normbb = boundingRect(contour);
        width = normbb.width();
        height = normbb.height();
      }

      if (width < minWidth || width > maxWidth) {
        continue;
      }
      if (width < minHeight || width > maxHeight) {
        continue;
      }

      final double area = contourArea(contour);
      if (area < minArea || area > maxArea) {
        continue;
      }
      if (arcLength(contour, true) < minPerimeter || arcLength(contour, true) > maxPerimeter) {
        continue;
      }

      convexHull(contour, hull);
      final double solidity = 100 * area / contourArea(hull);
      if (solidity < minSolidity || solidity > maxSolidity) {
        continue;
      }

      if (contour.rows() < minVertexCount || contour.rows() > maxVertexCount) {
        continue;
      }

      final double ratio = width / height;
      if (ratio < minRatio || ratio > maxRatio) {
        continue;
      }

      outputContours.put(filteredContourCount++, contour);
    }

    outputContours.resize(filteredContourCount);

    outputSocket.setValue(new ContoursReport(outputContours,
        inputSocket.getValue().get().getRows(), inputSocket.getValue().get().getCols()));
  }
}