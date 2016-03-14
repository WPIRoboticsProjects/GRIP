package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;

import java.io.InputStream;
import java.util.Optional;

/**
 * An {@link Operation} that takes in a list of contours and outputs a list of any contours in the input that match
 * all of several criteria.  Right now, the user can specify a minimum area, minimum perimeter, and ranges for width
 * and height.
 * <p>
 * This is useful because running a Find Contours on a real-life image typically leads to many small undesirable
 * contours from noise and small objects, as well as contours that do not meet the expected characteristics of the
 * feature we're actually looking for.  So, this operation can help narrow them down.
 */
public class CountContoursOperation implements Operation {

    private final SocketHint<ContoursReport> contoursHint = new SocketHint.Builder<>(ContoursReport.class)
            .identifier("Contours").initialValueSupplier(ContoursReport::new).build();

    private final SocketHint<Number> amountHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Amount", 0, 0, Integer.MAX_VALUE);

    @Override
    public String getName() {
        return "Count Contours";
    }

    @Override
    public String getDescription() {
        return "Find number of contours.";
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/grip.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, contoursHint)
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{new OutputSocket<>(eventBus, amountHint)};
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final InputSocket<ContoursReport> inputSocket = (InputSocket<ContoursReport>) inputs[0];

        long amount = inputSocket.getValue().get().getContours().size();

        final OutputSocket<Number> outputSocket = (OutputSocket<Number>) outputs[0];
        outputSocket.setValue(amount);
    }
}
