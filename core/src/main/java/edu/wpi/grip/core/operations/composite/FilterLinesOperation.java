package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Given a ListReport, filter only the lines that meet certain criteria.  This operation can be used to narrow down detected lines
 * to only relevant ones.
 */
public class FilterLinesOperation implements Operation {
    private final SocketHint<LinesReport> inputHint =
            new SocketHint.Builder(LinesReport.class).identifier("Lines").build();

    private final SocketHint<Number> minLengthHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Min Length", 20);

    private final SocketHint<LinesReport> outputHint =
            new SocketHint.Builder(LinesReport.class)
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
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/filter-lines.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, inputHint),
                new InputSocket<>(eventBus, minLengthHint),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{new OutputSocket<>(eventBus, outputHint)};
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final LinesReport inputLines = (LinesReport) inputs[0].getValue().get();
        final Number minLength = (Number) inputs[1].getValue().get();
        final double minLengthSquared = Math.pow(minLength.doubleValue(), 2);

        final OutputSocket<LinesReport> linesOutputSocket = (OutputSocket<LinesReport>) outputs[0];
        final LinesReport outputLines = linesOutputSocket.getValue().get();

        outputLines.setInput(inputLines.getInput());
        outputLines.setLines(inputLines.getLines().stream().filter(line ->
                line.lengthSquared() > minLengthSquared
        ).collect(Collectors.toList()));

        linesOutputSocket.setValue(outputLines);
    }
}
