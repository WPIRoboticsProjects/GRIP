package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import com.google.common.collect.ImmutableList;

import org.bytedeco.javacpp.opencv_core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.Point;
import static org.bytedeco.javacpp.opencv_core.Point2f;
import static org.bytedeco.javacpp.opencv_core.Scalar;
import static org.bytedeco.javacpp.opencv_core.bitwise_xor;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_TC89_KCOS;
import static org.bytedeco.javacpp.opencv_imgproc.CV_FILLED;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_EXTERNAL;
import static org.bytedeco.javacpp.opencv_imgproc.circle;
import static org.bytedeco.javacpp.opencv_imgproc.drawContours;
import static org.bytedeco.javacpp.opencv_imgproc.findContours;
import static org.bytedeco.javacpp.opencv_imgproc.pointPolygonTest;
import static org.bytedeco.javacpp.opencv_imgproc.watershed;

/**
 * GRIP {@link Operation} for
 * {@link org.bytedeco.javacpp.opencv_imgproc#watershed}.
 */
public class WatershedOperation implements Operation {

  public static final OperationDescription DESCRIPTION =
      OperationDescription.builder()
          .name("Watershed")
          .summary("Isolates overlapping objects from the background and each other")
          .category(OperationDescription.Category.FEATURE_DETECTION)
          .icon(Icon.iconStream("opencv"))
          .build();

  private final SocketHint<Mat> srcHint = SocketHints.Inputs.createMatSocketHint("Input", false);
  private final SocketHint<ContoursReport> contoursHint =
      new SocketHint.Builder<>(ContoursReport.class)
          .identifier("Contours")
          .initialValueSupplier(ContoursReport::new)
          .build();

  private final SocketHint<ContoursReport> outputHint =
      new SocketHint.Builder<>(ContoursReport.class)
          .identifier("Features")
          .initialValueSupplier(ContoursReport::new)
          .build();

  private final InputSocket<Mat> srcSocket;
  private final InputSocket<ContoursReport> contoursSocket;
  private final OutputSocket<ContoursReport> outputSocket;

  private static final int MAX_MARKERS = 253;
  private final List<Mat> markerPool;
  private final MatVector contour = new MatVector(); // vector with a single element
  private final Mat markers = new Mat();
  private final Mat output = new Mat();
  private final Point backgroundLabel = new Point();

  @SuppressWarnings("JavadocMethod")
  public WatershedOperation(InputSocket.Factory inputSocketFactory,
                            OutputSocket.Factory outputSocketFactory) {
    srcSocket = inputSocketFactory.create(srcHint);
    contoursSocket = inputSocketFactory.create(contoursHint);
    outputSocket = outputSocketFactory.create(outputHint);
    markerPool = ImmutableList.copyOf(
        Stream.generate(Mat::new).limit(MAX_MARKERS).collect(Collectors.toList())
    );
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        srcSocket,
        contoursSocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        outputSocket
    );
  }

  @Override
  public void perform() {
    final Mat input = srcSocket.getValue().get();
    if (input.type() != CV_8UC3) {
      throw new IllegalArgumentException("Watershed only works on 8-bit, 3-channel images");
    }

    final ContoursReport contourReport = contoursSocket.getValue().get();
    final MatVector contours = contourReport.getContours();

    if (contours.size() > MAX_MARKERS) {
      throw new IllegalArgumentException(
          "A maximum of " + MAX_MARKERS + " contours can be used as markers."
              + " Filter contours before connecting them to this operation if this keeps happening."
              + " The contours must also all be external; nested contours will not work");
    }

    markers.create(input.size(), CV_32SC1);
    output.create(input.size(), CV_8UC1);
    bitwise_xor(markers, markers, markers);
    bitwise_xor(output, output, output);

    // draw foreground markers (these have to be different colors)
    for (int i = 0; i < contours.size(); i++) {
      drawContours(markers, contours, i, Scalar.all(i + 1), CV_FILLED, LINE_8, null, 2, null);
    }

    // draw background marker a different color from the foreground markers
    findBackgroundMarker(markers, contours);
    circle(markers, backgroundLabel, 1, Scalar.WHITE, -1, LINE_8, 0);

    // Perform watershed
    watershed(input, markers);
    markers.convertTo(output, CV_8UC1);

    List<Mat> contourList = new ArrayList<>((int) contours.size());
    for (int i = 1; i < contours.size(); i++) {
      Mat dst = markerPool.get(i - 1);
      bitwise_xor(dst, dst, dst);
      output.copyTo(dst, opencv_core.equals(markers, i).asMat());
      findContours(dst, contour, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_TC89_KCOS);
      if (contour.size() < 1) {
        throw new IllegalArgumentException("No contours for marker");
      }
      contourList.add(contour.get(0).clone());
    }
    MatVector foundContours = new MatVector(contourList.toArray(new Mat[contourList.size()]));
    outputSocket.setValue(new ContoursReport(foundContours, output.rows(), output.cols()));
  }

  /**
   * Finds the first available point to place a background marker for the watershed operation.
   */
  private void findBackgroundMarker(Mat markers, MatVector contours) {
    final int cols = markers.cols();
    final int rows = markers.rows();
    final int minDist = 5;
    Point2f backgroundLabel = new Point2f();
    boolean found = false;
    // Don't place use a marker anywhere within 5 pixels of the edge of the image,
    // or within 5 pixels of a contour
    for (int x = minDist; x < cols - minDist && !found; x++) {
      for (int y = minDist; y < rows - minDist && !found; y++) {
        backgroundLabel.x(x);
        backgroundLabel.y(y);
        boolean isOpen = true;
        for (int c = 0; c < contours.size(); c++) {
          isOpen = pointPolygonTest(contours.get(c), backgroundLabel, true) <= -minDist;
          if (!isOpen) {
            // We know (x,y) is in a contour, don't need to check if it's in any others
            break;
          }
        }
        found = isOpen;
      }
    }
    if (!found) {
      // Should only happen if the image is clogged with contours
      backgroundLabel.deallocate();
      throw new IllegalStateException("Could not find a point for the background label");
    }
    setBackgroundLabel(backgroundLabel);
    backgroundLabel.deallocate();
  }

  private void setBackgroundLabel(Point2f p) {
    this.backgroundLabel.x((int) p.x());
    this.backgroundLabel.y((int) p.y());
  }

}
