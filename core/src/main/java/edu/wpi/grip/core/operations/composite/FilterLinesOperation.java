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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Given a ListReport, filter only the lines that meet certain criteria.  This operation can be used
 * to narrow down detected lines to only relevant ones.
 */
@Description(name = "Filter Lines",
             summary = "Filter only lines from a Find Lines operation that fit certain criteria",
             category = OperationCategory.FEATURE_DETECTION,
             iconName = "filter-lines")
public class FilterLinesOperation implements Operation {

  private final SocketHint<LinesReport> inputHint =
      new SocketHint.Builder<>(LinesReport.class).identifier("Lines").build();

  private final SocketHint<Number> minLengthHint = SocketHints.Inputs
      .createNumberSpinnerSocketHint("Min Length", 20);

  private final SocketHint<List<Number>> angleHint = SocketHints.Inputs
      .createNumberListRangeSocketHint("Angle", 0, 360);

  private final SocketHint<LinesReport> outputHint =
      new SocketHint.Builder<>(LinesReport.class)
          .identifier("Lines").initialValueSupplier(LinesReport::new).build();


  private final InputSocket<LinesReport> inputSocket;
  private final InputSocket<Number> minLengthSocket;
  private final InputSocket<List<Number>> angleSocket;

  private final OutputSocket<LinesReport> linesOutputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public FilterLinesOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    this.inputSocket = inputSocketFactory.create(inputHint);
    this.minLengthSocket = inputSocketFactory.create(minLengthHint);
    this.angleSocket = inputSocketFactory.create(angleHint);

    this.linesOutputSocket = outputSocketFactory.create(outputHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        minLengthSocket,
        angleSocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        linesOutputSocket
    );
  }

  @Override
  @SuppressWarnings("unchecked")
  public void perform() {
    final LinesReport inputLines = inputSocket.getValue().get();
    final double minLengthSquared = Math.pow(minLengthSocket.getValue().get().doubleValue(), 2);
    final double minAngle = angleSocket.getValue().get().get(0).doubleValue();
    final double maxAngle = angleSocket.getValue().get().get(1).doubleValue();

    List<LinesReport.Line> lines = inputLines.getLines().stream()
        .filter(line -> line.lengthSquared() >= minLengthSquared)
        .filter(line -> (line.angle() >= minAngle && line.angle() <= maxAngle)
            || (line.angle() + 180.0 >= minAngle && line.angle() + 180.0 <= maxAngle))
        .collect(Collectors.toList());

    linesOutputSocket.setValue(new LinesReport(inputLines.getLineSegmentDetector(), inputLines
        .getInput(), lines));
  }
}
