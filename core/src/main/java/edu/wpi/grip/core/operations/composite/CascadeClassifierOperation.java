package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import com.google.common.collect.ImmutableList;

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
public class CascadeClassifierOperation implements Operation {

  public static final OperationDescription DESCRIPTION =
      OperationDescription.builder()
          .name("Cascade Classifier")
          .summary("Runs a cascade classifier on an image")
          .icon(Icon.iconStream("opencv"))
          .category(OperationDescription.Category.FEATURE_DETECTION)
          .build();

  private final CascadeClassifier classifier;
  private String lastFile = "";

  private final SocketHint<Mat> imageHint =
      SocketHints.Inputs.createMatSocketHint("Image", false);
  private final SocketHint<String> filePathHint =
      SocketHints.Inputs.createTextSocketHint("Classifier file", lastFile);
  private final SocketHint<Number> scaleHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Scale factor", 1.1, 1.01, Double.MAX_VALUE);
  private final SocketHint<Number> minNeighborsHint =
      SocketHints.Inputs.createNumberSpinnerSocketHint("Min neighbors", 3, 0, Integer.MAX_VALUE);
  private final SocketHint<Size> minSizeHint =
      new SocketHint.Builder<>(Size.class)
          .identifier("Min size")
          .initialValue(new Size(0, 0))
          .build();
  private final SocketHint<Size> maxSizeHint =
      new SocketHint.Builder<>(Size.class)
          .identifier("Max size")
          .initialValue(new Size(0, 0))
          .build();
  private final SocketHint<RectsReport> outputHint =
      new SocketHint.Builder<>(RectsReport.class)
          .identifier("Detected areas")
          .initialValue(RectsReport.NIL)
          .build();

  private final InputSocket<Mat> imageSocket;
  private final InputSocket<String> filePath;
  private final InputSocket<Number> scaleSocket;
  private final InputSocket<Number> minNeighborsSocket;
  private final InputSocket<Size> minSizeSocket;
  private final InputSocket<Size> maxSizeSocket;
  private final OutputSocket<RectsReport> output;

  @SuppressWarnings("JavadocMethod")
  public CascadeClassifierOperation(InputSocket.Factory isf, OutputSocket.Factory osf) {
    classifier = new CascadeClassifier();
    imageSocket = isf.create(imageHint);
    filePath = isf.create(filePathHint);
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
        filePath,
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
    if (!imageSocket.getValue().isPresent() || !filePath.getValue().isPresent()) {
      return;
    }
    final String fileName = filePath.getValue().get();
    if (!fileName.equals(lastFile)) {
      // Don't load the same file multiple times in a row
      try {
        classifier.load(fileName);
        lastFile = fileName;
      } catch (RuntimeException e) {
        // Error with the config file, reset the classifier and throw an exception
        classifier.load(lastFile);
        throw new IllegalArgumentException("Invalid XML in configuration file", e);
      }
    }
    final Mat image = imageSocket.getValue().get();
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
    output.setValue(new RectsReport(image, rects));
  }

}
