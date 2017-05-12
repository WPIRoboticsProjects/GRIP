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

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Operation for identifying parts of an image with a cascade classifier.
 */
@Description(name = "Cascade Cassifier",
             summary = "Runs a Haar cascade classifier on an image",
             category = OperationCategory.FEATURE_DETECTION,
             iconName = "opencv")
public class CascadeClassifierOperation implements Operation {

  private final SocketHint<MatWrapper> imageHint =
      SocketHints.createImageSocketHint("Image");
  private final SocketHint<CascadeClassifier> classifierHint =
      new SocketHint.Builder<>(CascadeClassifier.class)
          .identifier("Classifier")
          .build();
  private final SocketHint<Number> scaleHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Scale factor", 1.1, 1.01, Double.MAX_VALUE);
  private final SocketHint<Number> minNeighborsHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min neighbors", 3, 0, Integer.MAX_VALUE);
  private final SocketHint<Size> minSizeHint =
      SocketHints.Inputs.createSizeSocketHint("Min size", true);
  private final SocketHint<Size> maxSizeHint =
      SocketHints.Inputs.createSizeSocketHint("Max size", true);
  private final SocketHint<RectsReport> outputHint =
      new SocketHint.Builder<>(RectsReport.class)
          .identifier("Detected areas")
          .initialValue(RectsReport.NIL)
          .build();

  private final InputSocket<MatWrapper> imageSocket;
  private final InputSocket<CascadeClassifier> classifierSocket;
  private final InputSocket<Number> scaleSocket;
  private final InputSocket<Number> minNeighborsSocket;
  private final InputSocket<Size> minSizeSocket;
  private final InputSocket<Size> maxSizeSocket;
  private final OutputSocket<RectsReport> output;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public CascadeClassifierOperation(InputSocket.Factory isf, OutputSocket.Factory osf) {
    imageSocket = isf.create(imageHint);
    classifierSocket = isf.create(classifierHint);
    scaleSocket = isf.create(scaleHint);
    minNeighborsSocket = isf.create(minNeighborsHint);
    minSizeSocket = isf.create(minSizeHint);
    maxSizeSocket = isf.create(maxSizeHint);
    output = osf.create(outputHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        imageSocket,
        classifierSocket,
        scaleSocket,
        minNeighborsSocket,
        minSizeSocket,
        maxSizeSocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        output
    );
  }

  @Override
  public void perform() {
    if (!imageSocket.getValue().isPresent() || !classifierSocket.getValue().isPresent()) {
      return;
    }
    final MatWrapper input = imageSocket.getValue().get();
    final Mat image = input.getCpu();
    if (image.empty() || image.channels() != 3) {
      throw new IllegalArgumentException("A cascade classifier needs a three-channel input");
    }
    final CascadeClassifier classifier = classifierSocket.getValue().get();
    final double scaleFactor = (double) scaleSocket.getValue().get();
    final int minNeighbors = minNeighborsSocket.getValue().get().intValue();
    final Size minSize = minSizeSocket.getValue().get();
    final Size maxSize = maxSizeSocket.getValue().get();
    RectVector detections = new RectVector();
    classifier.detectMultiScale(image, detections, scaleFactor, minNeighbors, 0, minSize, maxSize);
    List<Rect> rects = new ArrayList<>();
    for (int i = 0; i < detections.size(); i++) {
      rects.add(detections.get(i));
    }
    output.setValue(new RectsReport(input, rects));
  }

}
