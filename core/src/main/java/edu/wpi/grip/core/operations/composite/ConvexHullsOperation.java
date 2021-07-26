package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;

import org.bytedeco.opencv.opencv_core.MatVector;
import static org.bytedeco.opencv.global.opencv_imgproc.convexHull;

/**
 * An {@link Operation} that finds the convex hull of each of a list of contours. This can help
 * remove holes in detected shapes, making them easier to analyze.
 */
@Description(name = "Convex Hulls",
             summary = "Compute the convex hulls of contours",
             category = OperationCategory.FEATURE_DETECTION)
public class ConvexHullsOperation implements Operation {

  private final SocketHint<ContoursReport> contoursHint = new SocketHint.Builder<>(ContoursReport
      .class)
      .identifier("Contours").initialValueSupplier(ContoursReport::new).build();

  private final InputSocket<ContoursReport> inputSocket;
  private final OutputSocket<ContoursReport> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public ConvexHullsOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.inputSocket = inputSocketFactory.create(contoursHint);

    this.outputSocket = outputSocketFactory.create(contoursHint);
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
        outputSocket
    );
  }

  @Override
  @SuppressWarnings("unchecked")
  public void perform() {
    final MatVector inputContours = inputSocket.getValue().get().getContours();
    final MatVector outputContours = new MatVector(inputContours.size());

    for (int i = 0; i < inputContours.size(); i++) {
      convexHull(inputContours.get(i), outputContours.get(i));
    }

    outputSocket.setValue(new ContoursReport(outputContours,
        inputSocket.getValue().get().getRows(), inputSocket.getValue().get().getCols()));
  }
}
