package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.sockets.SocketHint;

import java.io.InputStream;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.MatVector;
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
    public Category getCategory() {
        return Category.FEATURE_DETECTION;
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
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Socket<ContoursReport> inputSocket = contoursHint.safeCastSocket(inputs[0]);
        final ContoursReport inputValue = contoursHint.retrieveValue(inputSocket);
        final MatVector inputContours = inputValue.getContours();
        final MatVector outputContours = new MatVector(inputContours.size());

        for (int i = 0; i < inputContours.size(); i++) {
            convexHull(inputContours.get(i), outputContours.get(i));
        }

        final Socket<ContoursReport> outputSocket = contoursHint.safeCastSocket(outputs[0]);
        outputSocket.setValue(new ContoursReport(outputContours, inputValue.getRows(), inputValue.getCols()));
    }
}
