package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.Description;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.JavaCppUtils;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_imgproc.arcLength;
import static org.bytedeco.javacpp.opencv_imgproc.boundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.contourArea;
import static org.bytedeco.javacpp.opencv_imgproc.convexHull;

/**
 * An {@link Operation} that takes in a list of contours and outputs a list of any contours in the
 * input that match all of several criteria.  Right now, the user can specify a minimum area,
 * minimum perimeter, and ranges for width and height. This is useful because running a Find
 * Contours on a real-life image typically leads to many small undesirable contours from noise and
 * small objects, as well as contours that do not meet the expected characteristics of the feature
 * we're actually looking for.  So, this operation can help narrow them down.
 */
@Description(name = "Filter Contours",
             summary = "Find contours matching certain criteria",
             category = OperationDescription.Category.FEATURE_DETECTION,
             iconName = "find-contours")
@SuppressWarnings("PMD.TooManyFields")
public class FilterContoursOperation implements Operation {

  private final SocketHint<ContoursReport> contoursHint = new SocketHint.Builder<>(ContoursReport
      .class)
      .identifier("Contours").initialValueSupplier(ContoursReport::new).build();

  private final SocketHint<Number> minAreaHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min Area", 0, 0, Integer.MAX_VALUE);

  private final SocketHint<Number> minPerimeterHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min Perimeter", 0, 0, Integer.MAX_VALUE);

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
  private final InputSocket<Number> minPerimeterSocket;
  private final InputSocket<Number> minWidthSocket;
  private final InputSocket<Number> maxWidthSocket;
  private final InputSocket<Number> minHeightSocket;
  private final InputSocket<Number> maxHeightSocket;
  private final InputSocket<List<Number>> soliditySocket;
  private final InputSocket<Number> minVertexSocket;
  private final InputSocket<Number> maxVertexSocket;
  private final InputSocket<Number> minRatioSocket;
  private final InputSocket<Number> maxRatioSocket;

  private final OutputSocket<ContoursReport> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public FilterContoursOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.contoursSocket = inputSocketFactory.create(contoursHint);
    this.minAreaSocket = inputSocketFactory.create(minAreaHint);
    this.minPerimeterSocket = inputSocketFactory.create(minPerimeterHint);
    this.minWidthSocket = inputSocketFactory.create(minWidthHint);
    this.maxWidthSocket = inputSocketFactory.create(maxWidthHint);
    this.minHeightSocket = inputSocketFactory.create(minHeightHint);
    this.maxHeightSocket = inputSocketFactory.create(maxHeightHint);
    this.soliditySocket = inputSocketFactory.create(solidityHint);
    this.minVertexSocket = inputSocketFactory.create(minVertexHint);
    this.maxVertexSocket = inputSocketFactory.create(maxVertexHint);
    this.minRatioSocket = inputSocketFactory.create(minRatioHint);
    this.maxRatioSocket = inputSocketFactory.create(maxRatioHint);

    this.outputSocket = outputSocketFactory.create(contoursHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        contoursSocket,
        minAreaSocket,
        minPerimeterSocket,
        minWidthSocket,
        maxWidthSocket,
        minHeightSocket,
        maxHeightSocket,
        soliditySocket,
        maxVertexSocket,
        minVertexSocket,
        minRatioSocket,
        maxRatioSocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        outputSocket
    );
  }

  public static boolean inRange(double x, double min, double max) {
    return x >= min && x <= max;
  }

  @Override
  public void perform() {
    final InputSocket<ContoursReport> inputSocket = contoursSocket;
    final double minArea = minAreaSocket.getValue().get().doubleValue();
    final double minPerimeter = minPerimeterSocket.getValue().get().doubleValue();
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
    final MatVector outputContours = JavaCppUtils.stream(inputContours)
        .filter(contour -> sizeMatch(contour, minWidth, maxWidth, minHeight, maxHeight))
        .filter(contour -> ratioMatch(contour, minRatio, maxRatio))
        .filter(contour -> arcLength(contour, true) >= minPerimeter)
        .filter(contour -> contourArea(contour) >= minArea)
        .filter(contour -> solidityInRange(contour, minSolidity, maxSolidity))
        .filter(contour -> inRange(contour.rows(), minVertexCount, maxVertexCount))
        .collect(JavaCppUtils.toMatVector());

    outputSocket.setValue(new ContoursReport(outputContours,
        inputSocket.getValue().get().getRows(), inputSocket.getValue().get().getCols()));
  }

  private static boolean sizeMatch(Mat contour,
                                   double minWidth, double maxWidth,
                                   double minHeight, double maxHeight) {
    try (Rect bb = boundingRect(contour)) {
      return inRange(bb.width(), minWidth, maxWidth)
          && inRange(bb.height(), minHeight, maxHeight);
    }
  }

  private static boolean solidityInRange(Mat contour, double minSolidity, double maxSolidity) {
    try (Mat hull = new Mat()) {
      convexHull(contour, hull);
      return inRange(100 * contourArea(contour) / contourArea(hull), minSolidity, maxSolidity);
    }
  }

  private static boolean ratioMatch(Mat contour, double minRatio, double maxRatio) {
    try (Rect bb = boundingRect(contour)) {
      return inRange(bb.width() / (double) bb.height(), minRatio, maxRatio);
    }
  }

}
