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
public class FilterContoursOperation implements Operation {

    private final SocketHint<ContoursReport> contoursHint = new SocketHint.Builder<>(ContoursReport.class)
            .identifier("Contours").initialValueSupplier(ContoursReport::new).build();

    private final SocketHint<Number> minAreaHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Min Area", 0, 0, Integer.MAX_VALUE);

    private final SocketHint<Number> minPerimeterHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Min Perimeter", 0, 0, Integer.MAX_VALUE);

    private final SocketHint<Number> minWidthHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Min Width", 0, 0, Integer.MAX_VALUE);

    private final SocketHint<Number> maxWidthHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Max Width", 1000, 0, Integer.MAX_VALUE);

    private final SocketHint<Number> minHeightHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Min Height", 0, 0, Integer.MAX_VALUE);

    private final SocketHint<Number> maxHeightHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Max Height", 1000, 0, Integer.MAX_VALUE);

    private final SocketHint<List> solidityHint =
            SocketHints.Inputs.createNumberListRangeSocketHint("Solidity", 0, 100);

    @Override
    public String getName() {
        return "Filter Contours";
    }

    @Override
    public String getDescription() {
        return "Find contours matching certain criteria.";
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
                new InputSocket<>(eventBus, minAreaHint),
                new InputSocket<>(eventBus, minPerimeterHint),
                new InputSocket<>(eventBus, minWidthHint),
                new InputSocket<>(eventBus, maxWidthHint),
                new InputSocket<>(eventBus, minHeightHint),
                new InputSocket<>(eventBus, maxHeightHint),
                new InputSocket<>(eventBus, solidityHint),
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
        final double minArea = ((Number) inputs[1].getValue().get()).doubleValue();
        final double minPerimeter = ((Number) inputs[2].getValue().get()).doubleValue();
        final double minWidth = ((Number) inputs[3].getValue().get()).doubleValue();
        final double maxWidth = ((Number) inputs[4].getValue().get()).doubleValue();
        final double minHeight = ((Number) inputs[5].getValue().get()).doubleValue();
        final double maxHeight = ((Number) inputs[6].getValue().get()).doubleValue();
        final double minSolidity = ((List<Number>) inputs[7].getValue().get()).get(0).doubleValue();
        final double maxSolidity = ((List<Number>) inputs[7].getValue().get()).get(1).doubleValue();

        final MatVector inputContours = inputSocket.getValue().get().getContours();
        final MatVector outputContours = new MatVector(inputContours.size());
        final Mat hull = new Mat();

        // Add contours from the input vector to the output vector only if they pass all of the criteria (minimum
        // area, minimum perimeter, width, and height, etc...)
        int filteredContourCount = 0;
        for (int i = 0; i < inputContours.size(); i++) {
            final Mat contour = inputContours.get(i);

            final Rect bb = boundingRect(contour);
            if (bb.width() < minWidth || bb.width() > maxWidth) continue;
            if (bb.height() < minHeight || bb.height() > maxHeight) continue;

            final double area = contourArea(contour);
            if (area < minArea) continue;
            if (arcLength(contour, true) < minPerimeter) continue;

            convexHull(contour, hull);
            final double solidity = 100 * area / contourArea(hull);
            if (solidity < minSolidity || solidity > maxSolidity) continue;

            outputContours.put(filteredContourCount++, contour);
        }

        outputContours.resize(filteredContourCount);

        final OutputSocket<ContoursReport> outputSocket = (OutputSocket<ContoursReport>) outputs[0];
        outputSocket.setValue(new ContoursReport(outputContours,
                inputSocket.getValue().get().getRows(), inputSocket.getValue().get().getCols()));
    }
}
