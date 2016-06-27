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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.CV_8U;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.Point;
import static org.bytedeco.javacpp.opencv_core.Scalar;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_TC89_KCOS;
import static org.bytedeco.javacpp.opencv_imgproc.CV_FILLED;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_EXTERNAL;
import static org.bytedeco.javacpp.opencv_imgproc.circle;
import static org.bytedeco.javacpp.opencv_imgproc.drawContours;
import static org.bytedeco.javacpp.opencv_imgproc.findContours;
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

  @SuppressWarnings("unchecked")
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

    final Mat markers = new Mat(input.size(), CV_32SC1, new Scalar(0.0));
    final Mat output = new Mat(markers.size(), CV_8UC1, new Scalar(0.0));

    try {
      // draw foreground markers (these have to be different colors)
      for (int i = 0; i < contours.size(); i++) {
        drawContours(markers, contours, i, Scalar.all(i + 1), CV_FILLED, LINE_8, null, 2, null);
      }

      // draw background marker a different color from the foreground markers
      // TODO maybe make this configurable? There may be something in the corner
      circle(markers, new Point(5, 5), 3, Scalar.WHITE, -1, LINE_8, 0);

      watershed(input, markers);

      // Create a new mat for each feature
      markers.convertTo(output, CV_8UC1);
      ByteBuffer buffer = output.createBuffer();
      final int stride = buffer.capacity() / output.rows();
      Map<Byte, Mat> segments = new LinkedHashMap<>(); // Map segment number to it's image
      int bufIdx;
      for (int y = 0; y < output.rows(); y++) {
        for (int x = 0; x < output.cols(); x++) {
          bufIdx = (stride * y) + (x * markers.channels());
          byte val = buffer.get(bufIdx);
          if (val == 0 || val == -1) {
            // Background (0) or border (-1)
            continue;
          }
          Mat m = segments.computeIfAbsent(val, k -> {
            Mat segment = new Mat();
            segment.create(markers.rows(), markers.cols(), CV_8U);
            segment.<ByteBuffer>createBuffer().put(new byte[markers.rows() * markers.cols()]);
            return segment;
          });
          ByteBuffer b = m.createBuffer();
          b.put(bufIdx, Byte.MAX_VALUE);
        }
      }

      // Find the contours of the segmented features
      MatVector segmentContours = new MatVector(segments.size());
      segments.forEach((which, image) -> {
        // This vector is guaranteed to contain exactly one contour
        MatVector theseContours = new MatVector();
        findContours(image, theseContours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_TC89_KCOS);
        Mat contour = theseContours.get(0);
        segmentContours.put(which - 1, contour);
      });

      outputSocket.setValue(new ContoursReport(segmentContours, markers.rows(), markers.cols()));
    } finally {
      // make sure that the working mat is freed to avoid a memory leak
      markers.release();
    }
  }

}
