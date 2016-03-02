package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * An {@link Operation} that takes in a list of contours and outputs a list of any contours in the input that match
 * all of several criteria.  Right now, the user can specify a minimum area, minimum perimeter, and ranges for width
 * and height.
 * <p>
 * This is useful because running a Find Contours on a real-life image typically leads to many small undesirable
 * contours from noise and small objects, as well as contours that do not meet the expected characteristics of the
 * feature we're actually looking for.  So, this operation can help narrow them down.
 */
public class AdvancedFilterContoursOperation implements Operation {

    private final SocketHint<ContoursReport> contoursHint = new SocketHint.Builder<>(ContoursReport.class)
            .identifier("Contours").initialValueSupplier(ContoursReport::new).build();

    private final SocketHint<Number> minVertexHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Min Vertex Count", 0, 0, Integer.MAX_VALUE);

    private final SocketHint<Number> maxVertexHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Max Vertex Count", 0, 1000, Integer.MAX_VALUE);

    private final SocketHint<Boolean> forceConvexHint =
            SocketHints.createBooleanSocketHint("Force Convex", false);

    private final SocketHint<Number> minRatioHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Min Ratio", 0, 0, Integer.MAX_VALUE);

    private final SocketHint<Number> maxRatioHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Max Ratio", 1000, 0, Integer.MAX_VALUE);

    @Override
    public String getName() {
        return "Advanced Filter Contours";
    }

    @Override
    public String getDescription() {
        return "Find contours matching certain advanced criteria.";
    }

    @Override
    public Category getCategory() {
        return Category.FEATURE_DETECTION;
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/find-contours.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, contoursHint),
                new InputSocket<>(eventBus, minVertexHint),
                new InputSocket<>(eventBus, maxVertexHint),
                new InputSocket<>(eventBus, forceConvexHint),
                new InputSocket<>(eventBus, minRatioHint),
                new InputSocket<>(eventBus, maxRatioHint),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{new OutputSocket<>(eventBus, contoursHint)};
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final InputSocket<ContoursReport> inputSocket = (InputSocket<ContoursReport>) inputs[0];
        final double minVertexCount = ((Number) inputs[1].getValue().get()).doubleValue();
        final double maxVertexCount = ((Number) inputs[2].getValue().get()).doubleValue();
        final boolean forceConvex = ((Boolean) inputs[3].getValue().get()).booleanValue();
        final double minRatio = ((Number) inputs[4].getValue().get()).doubleValue();
        final double maxRatio = ((Number) inputs[5].getValue().get()).doubleValue();

        final MatVector inputContours = inputSocket.getValue().get().getContours();
        final MatVector outputContours = new MatVector(inputContours.size());

        // Add contours from the input vector to the output vector only if they pass all of the criteria (minimum
        // area, minimum perimeter, width, and height, etc...)
        int filteredContourCount = 0;
        for (int i = 0; i < inputContours.size(); i++) {
            final Mat contour = inputContours.get(i);

            if(contour.rows() < minVertexCount || contour.rows() > maxVertexCount) continue;

            if(forceConvex && !isContourConvex(contour)) continue;

            final Rect bb = boundingRect(contour);
            final double ratio = bb.width() / bb.height();
            if (ratio < minRatio || ratio > maxRatio) continue;

            outputContours.put(filteredContourCount++, contour);
        }

        outputContours.resize(filteredContourCount);

        final OutputSocket<ContoursReport> outputSocket = (OutputSocket<ContoursReport>) outputs[0];
        outputSocket.setValue(new ContoursReport(outputContours,
                inputSocket.getValue().get().getRows(), inputSocket.getValue().get().getCols()));
    }
}
