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
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_imgproc.arcLength;
import static org.bytedeco.javacpp.opencv_imgproc.boundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.contourArea;

/**
 * An {@link Operation} that takes in a list of contours and outputs a list of any contours in the
 * input that match all of several criteria.  Right now, the user can specify a minimum area,
 * minimum perimeter, and ranges for width and height. This is useful because running a Find
 * Contours on a real-life image typically leads to many small undesirable contours from noise and
 * small objects, as well as contours that do not meet the expected characteristics of the feature
 * we're actually looking for.  So, this operation can help narrow them down.
 */
public class SimpleFilterContoursOperation implements Operation {

  public static final OperationDescription DESCRIPTION =
      OperationDescription.builder()
          .name("Simple Filter Contours")
          .summary("Find contours matching certain criteria")
          .category(OperationDescription.Category.FEATURE_DETECTION)
          .icon(Icon.iconStream("find-contours"))
          .build();

  private final SocketHint<ContoursReport> contoursHint = new SocketHint.Builder<>(ContoursReport
      .class)
      .identifier("Contours").initialValueSupplier(ContoursReport::new).build();

  private final SocketHint<Number> minAreaHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min Area", 0, 0, Integer.MAX_VALUE);

  private final SocketHint<Number> minPerimeterHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min Perimeter", 0, 0, Integer.MAX_VALUE);


  private final InputSocket<ContoursReport> contoursSocket;
  private final InputSocket<Number> minAreaSocket;
  private final InputSocket<Number> minPerimeterSocket;

  private final OutputSocket<ContoursReport> outputSocket;

  @SuppressWarnings("JavadocMethod")
  public SimpleFilterContoursOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.contoursSocket = inputSocketFactory.create(contoursHint);
    this.minAreaSocket = inputSocketFactory.create(minAreaHint);
    this.minPerimeterSocket = inputSocketFactory.create(minPerimeterHint);

    this.outputSocket = outputSocketFactory.create(contoursHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        contoursSocket,
        minAreaSocket,
        minPerimeterSocket
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
    final double minPerimeter = minPerimeterSocket.getValue().get().doubleValue();


    final MatVector inputContours = inputSocket.getValue().get().getContours();
    final MatVector outputContours = new MatVector(inputContours.size());
    final Mat hull = new Mat();

    // Add contours from the input vector to the output vector only if they pass all of the
    // criteria (minimum
    // area, minimum perimeter, width, and height, etc...)
    int filteredContourCount = 0;
    for (int i = 0; i < inputContours.size(); i++) {
      final Mat contour = inputContours.get(i);

      final Rect bb = boundingRect(contour);

      final double area = contourArea(contour);
      if (area < minArea) {
        continue;
      }
      if (arcLength(contour, true) < minPerimeter) {
        continue;
      }
      outputContours.put(filteredContourCount++, contour);
    }

    outputContours.resize(filteredContourCount);

    outputSocket.setValue(new ContoursReport(outputContours,
        inputSocket.getValue().get().getRows(), inputSocket.getValue().get().getCols()));
  }
}
