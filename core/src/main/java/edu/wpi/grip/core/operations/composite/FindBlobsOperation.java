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

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.KeyPoint;
import static org.bytedeco.javacpp.opencv_core.KeyPointVector;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_features2d.SimpleBlobDetector;

/**
 * Find groups of similar pixels in a color or grayscale image.
 */
@Description(name = "Find Blobs",
             summary = "Detects groups of pixels in an image",
             category = OperationCategory.FEATURE_DETECTION,
             iconName = "find-blobs")
public class FindBlobsOperation implements Operation {

  private final SocketHint<MatWrapper> inputHint = SocketHints.createImageSocketHint("Input");
  private final SocketHint<Number> minAreaHint = SocketHints.Inputs
      .createNumberSpinnerSocketHint("Min Area", 1);
  private final SocketHint<List<Number>> circularityHint = SocketHints.Inputs
      .createNumberListRangeSocketHint("Circularity", 0.0, 1.0);
  private final SocketHint<List<Number>> convexityHint = SocketHints.Inputs
      .createNumberListRangeSocketHint("Convexity", 0.0, 1.0);
  private final SocketHint<List<Number>> inertiaRatioHint = SocketHints.Inputs
      .createNumberListRangeSocketHint("Inertia Ratio", 0.0, 1.0);
  private final SocketHint<Boolean> colorHint = SocketHints
      .createBooleanSocketHint("Dark Blobs", false);

  private final SocketHint<BlobsReport> blobsHint = new SocketHint.Builder<>(BlobsReport.class)
      .identifier("Blobs")
      .initialValueSupplier(BlobsReport::new)
      .build();

  private final InputSocket<MatWrapper> inputSocket;
  private final InputSocket<Number> minAreaSocket;
  private final InputSocket<List<Number>> circularitySocket;
  private final InputSocket<List<Number>> convexitySocket;
  private final InputSocket<List<Number>> inertiaRatioSocket;
  private final InputSocket<Boolean> colorSocket;

  private final OutputSocket<BlobsReport> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public FindBlobsOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.inputSocket = inputSocketFactory.create(inputHint);
    this.minAreaSocket = inputSocketFactory.create(minAreaHint);
    this.circularitySocket = inputSocketFactory.create(circularityHint);
    this.colorSocket = inputSocketFactory.create(colorHint);
    this.convexitySocket = inputSocketFactory.create(convexityHint);
    this.inertiaRatioSocket = inputSocketFactory.create(inertiaRatioHint);

    this.outputSocket = outputSocketFactory.create(blobsHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        minAreaSocket,
        circularitySocket,
        colorSocket,
        //Sockets placed last to maintain backwards compatibility in deserialization
        convexitySocket,
        inertiaRatioSocket
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
    final Mat input = inputSocket.getValue().get().getCpu();
    final Number minArea = minAreaSocket.getValue().get();
    final List<Number> circularity = circularitySocket.getValue().get();
    final Boolean darkBlobs = colorSocket.getValue().get();
    final List<Number> convexity = convexitySocket.getValue().get();
    final List<Number> inertiaRatio = inertiaRatioSocket.getValue().get();

    final SimpleBlobDetector blobDetector = SimpleBlobDetector.create(new SimpleBlobDetector
        .Params()
        .filterByArea(true)
        .minArea(minArea.intValue())
        .maxArea(Integer.MAX_VALUE)

        .filterByColor(true)
        .blobColor(darkBlobs ? (byte) 0 : (byte) 255)

        .filterByCircularity(true)
        .minCircularity(circularity.get(0).floatValue())
        .maxCircularity(circularity.get(1).floatValue())

        .filterByConvexity(true)
        .minConvexity(convexity.get(0).floatValue())
        .maxConvexity(convexity.get(1).floatValue())

        .filterByInertia(true)
        .minInertiaRatio(inertiaRatio.get(0).floatValue())
        .maxInertiaRatio(inertiaRatio.get(1).floatValue()));

    // Detect the blobs and store them in the output BlobsReport
    final KeyPointVector keyPointVector = new KeyPointVector();
    blobDetector.detect(input, keyPointVector);

    final List<BlobsReport.Blob> blobs = new ArrayList<>();
    for (int i = 0; i < keyPointVector.size(); i++) {
      final KeyPoint keyPoint = keyPointVector.get(i);
      blobs.add(new BlobsReport.Blob(keyPoint.pt().x(), keyPoint.pt().y(), keyPoint.size()));
    }

    outputSocket.setValue(new BlobsReport(inputSocket.getValue().get(), blobs));
  }
}
