package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import com.google.common.collect.ImmutableList;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

  @SuppressWarnings("JavadocMethod")
  public WatershedOperation(InputSocket.Factory inputSocketFactory,
                            OutputSocket.Factory outputSocketFactory) {
    srcSocket = inputSocketFactory.create(srcHint);
    contoursSocket = inputSocketFactory.create(contoursHint);
    outputSocket = outputSocketFactory.create(outputHint);
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

    final int maxMarkers = 253;
    if (contours.size() > maxMarkers) {
      throw new IllegalArgumentException(
          "A maximum of " + maxMarkers + " contours can be used as markers."
              + " Filter contours before connecting them to this operation if this keeps happening."
              + " The contours must also all be external; nested contours will not work");
    }

    final Mat markers = new Mat(input.size(), CV_32SC1, new Scalar(0.0));
    final Mat output = new Mat(markers.size(), CV_8UC1, new Scalar(0.0));

    try {
      // draw foreground markers (these have to be different colors)
      for (int i = 0; i < contours.size(); i++) {
        drawContours(markers, contours, i, Scalar.all(i + 1), CV_FILLED, LINE_8, null, 2, null);
      }

      // draw background marker a different color from the foreground markers
      Point backgroundLabel = fromPoint2f(findBackgroundMarker(markers, contours));
      circle(markers, backgroundLabel, 1, Scalar.WHITE, -1, LINE_8, 0);

      // Perform watershed
      watershed(input, markers);
      markers.convertTo(output, CV_8UC1);

      // Create a new mat for each feature
      final ByteBuffer buffer = output.createBuffer();
      final Map<Byte, Mat> segments = new HashMap<>(); // Map segment number to its image
      final Map<Byte, ByteBuffer> buffers = new HashMap<>(); // avoid creating tons of buffers
      final int rows = output.rows();
      final int cols = output.cols();
      // Don't replace these functions with lambdas
      final Function<Byte, Mat> newMat = k -> {
        Mat segment = new Mat(rows, cols, CV_8UC1);
        bitwise_xor(segment, segment, segment);
        return segment;
      };
      final Function<Byte, ByteBuffer> newBuf = k -> segments.get(k).createBuffer();
      for (int i = 0; i < rows * cols; i++) {
        byte val = buffer.get(i);
        if (val == 0 || val == -1) {
          // Background (0) or border (-1)
          continue;
        }
        segments.computeIfAbsent(val, newMat);
        buffers.computeIfAbsent(val, newBuf).put(i, Byte.MAX_VALUE);
      }

      // Should have one segmented image per contour
      if (segments.size() != contours.size()) {
        throw new IllegalStateException(
            segments.size() + " != " + contours.size() + ". Are the contours nested?");
      }

      // Find the contours of the segmented features
      MatVector segmentContours = new MatVector(segments.size());
      segments.forEach((which, image) -> {
        int index = which & 0xFF; // convert signed byte to unsigned
        if (index < 1 || index > segmentContours.size()) {
          throw new IndexOutOfBoundsException(
              index + " < 1 or " + index + " > " + segmentContours.size());
        }
        // This vector is guaranteed to contain exactly one contour
        MatVector theseContours = new MatVector();
        findContours(image, theseContours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_TC89_KCOS);
        Mat contour = theseContours.get(0);
        segmentContours.put(index - 1, contour);
        image.release();
      });

      outputSocket.setValue(new ContoursReport(segmentContours, rows, cols));
    } finally {
      // make sure that the working mat is freed to avoid a memory leak
      markers.release();
    }
  }

  /**
   * Finds the first available point to place a background marker for the watershed operation.
   */
  private Point2f findBackgroundMarker(Mat markers, MatVector contours) {
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
      throw new IllegalStateException("Could not find a point for the background label");
    }
    return backgroundLabel;
  }

  private Point fromPoint2f(Point2f p) {
    return new Point((int) p.x(), (int) p.y());
  }

}
