package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;

import org.bytedeco.opencv.opencv_core.Rect;


/**
 * Crop an image to an exact width and height using one of several origin modes.  Cropping
 * images down can be a useful optimization.
 */
@Description(name = "Crop",
             summary = "Crop an image to an exact size",
             category = OperationCategory.IMAGE_PROCESSING,
             iconName = "crop")
public class CropOperation implements Operation {

  private final InputSocket<MatWrapper> inputSocket;
  private final InputSocket<Number> xSocket;
  private final InputSocket<Number> ySocket;

  private final InputSocket<Number> widthSocket;
  private final InputSocket<Number> heightSocket;
  private final InputSocket<Origin> originSocket;

  private final OutputSocket<MatWrapper> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public CropOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.inputSocket = inputSocketFactory.create(SocketHints
        .createImageSocketHint("Input"));
    this.xSocket = inputSocketFactory.create(SocketHints.Inputs
        .createNumberSpinnerSocketHint("X", 100));
    this.ySocket = inputSocketFactory.create(SocketHints.Inputs
        .createNumberSpinnerSocketHint("Y", 100));
    this.widthSocket = inputSocketFactory.create(SocketHints.Inputs
        .createNumberSpinnerSocketHint("Width", 50));
    this.heightSocket = inputSocketFactory.create(SocketHints.Inputs
        .createNumberSpinnerSocketHint("Height", 50));
    this.originSocket = inputSocketFactory
        .create(SocketHints.createEnumSocketHint("Origin", Origin.CENTER));

    this.outputSocket = outputSocketFactory.create(SocketHints
        .createImageSocketHint("Output"));
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        xSocket,
        ySocket,
        widthSocket,
        heightSocket,
        originSocket
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
    final MatWrapper input = inputSocket.getValue().get();
    final MatWrapper output = outputSocket.getValue().get();
    final Number x = xSocket.getValue().get();
    final Number y = ySocket.getValue().get();
    final Number width = widthSocket.getValue().get();
    final Number height = heightSocket.getValue().get();

    final Origin origin = originSocket.getValue().get();

    final Rect regionOfInterest = new Rect(
            x.intValue() + (int) (origin.xOffsetMultiplier * width.intValue()),
            y.intValue() + (int) (origin.yOffsetMultiplier * height.intValue()),
            width.intValue(),
            height.intValue()
            );

    //apply() returns a sub-matrix; It does not modify the input Mat: https://github.com/WPIRoboticsProjects/GRIP/pull/926
    if (input.isCpu()) {
      output.set(input.getCpu().apply(regionOfInterest));
    } else {
      output.set(input.getGpu().apply(regionOfInterest));
    }

    outputSocket.setValue(output);
  }

  private enum Origin {
    TOP_LEFT("Top Left", 0, 0),
    TOP_RIGHT("Top Right", -1, 0),
    BOTTOM_LEFT("Bottom Left", 0, -1),
    BOTTOM_RIGHT("Bottom Right", -1, -1),
    CENTER("Center", -.5, -.5);

    final String label;
    final double xOffsetMultiplier;
    final double yOffsetMultiplier;

    Origin(String label, double xOffsetMultiplier, double yOffsetMultiplier) {
      this.label = label;
      this.xOffsetMultiplier = xOffsetMultiplier;
      this.yOffsetMultiplier = yOffsetMultiplier;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
