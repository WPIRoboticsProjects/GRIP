package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_TC89_KCOS;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_EXTERNAL;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_LIST;
import static org.bytedeco.javacpp.opencv_imgproc.findContours;

/**
 * An {@link Operation} that, given a binary image, produces a list of contours of all of the shapes
 * in the image.
 */
@Description(name = "Find Contours",
             summary = "Detects contours in a binary image",
             category = OperationCategory.FEATURE_DETECTION,
             iconName = "find-contours")
public class FindContoursOperation implements Operation {

  private final SocketHint<MatWrapper> inputHint = SocketHints.createImageSocketHint("Input");

  private final SocketHint<Boolean> externalHint =
      SocketHints.createBooleanSocketHint("External Only", false);

  private final SocketHint<ContoursReport> contoursHint = new SocketHint.Builder<>(ContoursReport
      .class)
      .identifier("Contours").initialValueSupplier(ContoursReport::new).build();


  private final InputSocket<MatWrapper> inputSocket;
  private final InputSocket<Boolean> externalSocket;

  private final OutputSocket<ContoursReport> contoursSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public FindContoursOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.inputSocket = inputSocketFactory.create(inputHint);
    this.externalSocket = inputSocketFactory.create(externalHint);

    this.contoursSocket = outputSocketFactory.create(contoursHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        externalSocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        contoursSocket
    );
  }

  @Override
  public void perform() {
    final Mat input = inputSocket.getValue().get().getCpu();
    if (input.empty()) {
      return;
    }

    final Mat tmp = new Mat();
    final boolean externalOnly = externalSocket.getValue().get();

    // findContours modifies its input, so we pass it a temporary copy of the input image
    input.copyTo(tmp);

    // OpenCV has a few different things it can return from findContours, but for now we only use
    // EXTERNAL and LIST.
    // The other ones involve hierarchies of contours, which might be useful in some situations,
    // but probably only
    // when processing the contours manually in code (so, not in a graphical pipeline).
    MatVector contours = new MatVector();
    findContours(tmp, contours, externalOnly ? CV_RETR_EXTERNAL : CV_RETR_LIST,
        CV_CHAIN_APPROX_TC89_KCOS);
    tmp.release();

    contoursSocket.setValue(new ContoursReport(contours, input.rows(), input.cols()));
  }
}
