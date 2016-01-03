package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;

import java.io.InputStream;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_imgproc.convexHull;

/**
 * An {@link Operation} that finds the convex hull of each of a list of contours.
 * <p>
 * This can help remove holes in detected shapes, making them easier to analyze.
 */
public class ConvexHullsOperation implements Operation {
    private final SocketHint<ContoursReport> contoursHint = new SocketHint.Builder<>(ContoursReport.class)
            .identifier("Contours").initialValueSupplier(ContoursReport::new).build();

    @Override
    public String getName() {
        return "Convex Hulls";
    }

    @Override
    public String getDescription() {
        return "Compute the convex hulls of contours.";
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/convex-hulls.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{new InputSocket<>(eventBus, contoursHint)};
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{new OutputSocket<>(eventBus, contoursHint)};
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final InputSocket<ContoursReport> inputSocket = (InputSocket<ContoursReport>) inputs[0];
        final OutputSocket<ContoursReport> outputSocket = (OutputSocket<ContoursReport>) outputs[0];

        final ContoursReport inputContours = inputSocket.getValue().get();
        final ContoursReport outputContours = outputSocket.getValue().get();
        outputContours.getContours().resize(inputContours.getContours().size());

        for (int i = 0; i < inputContours.getContours().size(); i++) {
            convexHull(inputContours.getContours().get(i), outputContours.getContours().get(i));
        }

        outputContours.setRows(inputContours.getRows());
        outputContours.setCols(inputContours.getCols());
        outputSocket.setValue(outputContours);
    }
}
