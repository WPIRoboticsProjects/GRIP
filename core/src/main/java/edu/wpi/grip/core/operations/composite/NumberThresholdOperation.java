package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;

import java.io.InputStream;
import java.util.Optional;

/**
 * @author Jaxon A Brown
 */
public class NumberThresholdOperation implements Operation {
    private final SocketHint<Number> numHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Input", 0);
    private final SocketHint<Number> minHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Min", -1);
    private final SocketHint<Number> maxHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Max", 1);

    private final SocketHint<Boolean> boolHint = SocketHints.Outputs.createBooleanSocketHint("Output", true);

    @Override
    public String getName() {
        return "NumberThreshold";
    }

    @Override
    public String getDescription() {
        return "Returns a boolean on weather or not the number is within the given range.";
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/grip.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[] {
                new InputSocket<>(eventBus, numHint),
                new InputSocket<>(eventBus, minHint),
                new InputSocket<>(eventBus, maxHint),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket[] {
                new OutputSocket<>(eventBus, boolHint)
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Number num = ((InputSocket<Number>) inputs[0]).getValue().get();
        final Number min = ((InputSocket<Number>) inputs[1]).getValue().get();
        final Number max = ((InputSocket<Number>) inputs[2]).getValue().get();

        final OutputSocket<Boolean> outputSocket = (OutputSocket<Boolean>) outputs[0];

        Boolean output;
        if(min.doubleValue() <= num.doubleValue() && max.doubleValue() >= num.doubleValue()) {
            output = true;
        } else {
            output = false;
        }

        outputSocket.setValue(output);
    }
}
