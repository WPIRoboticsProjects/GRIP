package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.bytedeco.javacpp.indexer.FloatIndexer;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.LineSegmentDetector;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * Find line segments in a color or grayscale image.
 */
@Description(name = "Find Lines",
             summary = "Detects line segments in an image",
             category = OperationCategory.FEATURE_DETECTION,
             iconName = "find-lines")
public class FindLinesOperation implements Operation {

  private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
  private final SocketHint<LinesReport> linesHint = new SocketHint.Builder<>(LinesReport.class)
      .identifier("Lines").initialValueSupplier(LinesReport::new).build();


  private final InputSocket<Mat> inputSocket;

  private final OutputSocket<LinesReport> linesReportSocket;

  @Inject
  public FindLinesOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.inputSocket = inputSocketFactory.create(inputHint);
    this.linesReportSocket = outputSocketFactory.create(linesHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        linesReportSocket
    );
  }

  @Override
  @SuppressWarnings("unchecked")
  public void perform() {
    final Mat input = inputSocket.getValue().get();
    final LineSegmentDetector lsd = linesReportSocket.getValue().get().getLineSegmentDetector();

    final Mat lines = new Mat();
    if (input.channels() == 1) {
      lsd.detect(input, lines);
    } else {
      // The line detector works on a single channel.  If the input is a color image, we can just
      // give the line  detector a grayscale version of it
      final Mat tmp = new Mat();
      cvtColor(input, tmp, COLOR_BGR2GRAY);
      lsd.detect(tmp, lines);
      tmp.release();
    }

    // Store the lines in the LinesReport object
    List<LinesReport.Line> lineList = new ArrayList<>();
    if (!lines.empty()) {
      final FloatIndexer indexer = lines.createIndexer();
      final float[] tmp = new float[4];
      for (int i = 0; i < lines.rows(); i++) {
        indexer.get(i, tmp);
        lineList.add(new LinesReport.Line(tmp[0], tmp[1], tmp[2], tmp[3]));
      }
    }
    lines.release();

    linesReportSocket.setValue(new LinesReport(lsd, input, lineList));
  }
}
