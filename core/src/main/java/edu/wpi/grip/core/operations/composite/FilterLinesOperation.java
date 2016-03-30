package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.sockets.*;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Given a ListReport, filter only the lines that meet certain criteria.  This operation can be used to narrow down detected lines
 * to only relevant ones.
 */
public class FilterLinesOperation implements Operation {
    private final SocketHint<LinesReport> inputHint =
            new SocketHint.Builder<>(LinesReport.class).identifier("Lines").build();

    private final SocketHint<Number> minLengthHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Min Length", 20);

    private final SocketHint<List<Number>> angleHint = SocketHints.Inputs.createNumberListRangeSocketHint("Angle", 0, 360);

    private final SocketHint<LinesReport> outputHint =
            new SocketHint.Builder<>(LinesReport.class)
                    .identifier("Lines").initialValueSupplier(LinesReport::new).build();

    @Override
    public String getName() {
        return "Filter Lines";
    }

    @Override
    public String getDescription() {
        return "Filter only lines from a Find Lines operation that fit certain criteria.";
    }

    @Override
    public Category getCategory() {
        return Category.FEATURE_DETECTION;
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/filter-lines.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, inputHint),
                new InputSocket<>(eventBus, minLengthHint),
                new InputSocket<>(eventBus, angleHint),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{new OutputSocket<>(eventBus, outputHint)};
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final LinesReport inputLines = inputHint.retrieveValue(inputs[0]);
        final double minLengthSquared = Math.pow(minLengthHint.retrieveValue(inputs[1]).doubleValue(), 2);
        final double minAngle = angleHint.retrieveValue(inputs[2]).get(0).doubleValue();
        final double maxAngle = angleHint.retrieveValue(inputs[2]).get(1).doubleValue();

        final Socket<LinesReport> linesOutputSocket = outputHint.safeCastSocket(outputs[0]);

        final List<LinesReport.Line> lines = inputLines.getLines().stream()
                .filter(line -> line.lengthSquared() >= minLengthSquared)
                .filter(line -> (line.angle() >= minAngle && line.angle() <= maxAngle)
                        || (line.angle() + 180.0 >= minAngle && line.angle() + 180.0 <= maxAngle))
                .collect(Collectors.toList());

        linesOutputSocket.setValue(new LinesReport(inputLines.getLineSegmentDetector(), inputLines.getInput(), lines));
    }
}
