package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.sockets.*;

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

    private final SocketHint<List<Number>> solidityHint =
            SocketHints.Inputs.createNumberListRangeSocketHint("Solidity", 0, 100);

    private final SocketHint<Number> minVertexHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Min Vertices", 0, 0, Integer.MAX_VALUE);

    private final SocketHint<Number> maxVertexHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Max Vertices", 1000000, 0, Integer.MAX_VALUE);

    private final SocketHint<Number> minRatioHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Min Ratio", 0, 0, Integer.MAX_VALUE);

    private final SocketHint<Number> maxRatioHint =
            SocketHints.Inputs.createNumberSpinnerSocketHint("Max Ratio", 1000, 0, Integer.MAX_VALUE);


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
                new InputSocket<>(eventBus, minVertexHint),
                new InputSocket<>(eventBus, maxVertexHint),
                new InputSocket<>(eventBus, minRatioHint),
                new InputSocket<>(eventBus, maxRatioHint),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{new OutputSocket<>(eventBus, contoursHint)};
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Socket<ContoursReport> inputSocket = contoursHint.safeCastSocket(inputs[0]);
        final double minArea = minAreaHint.retrieveValue(inputs[1]).doubleValue();
        final double minPerimeter = minPerimeterHint.retrieveValue(inputs[2]).doubleValue();
        final double minWidth = minWidthHint.retrieveValue(inputs[3]).doubleValue();
        final double maxWidth = maxWidthHint.retrieveValue(inputs[4]).doubleValue();
        final double minHeight = minHeightHint.retrieveValue(inputs[5]).doubleValue();
        final double maxHeight = maxHeightHint.retrieveValue(inputs[6]).doubleValue();
        final double minSolidity = solidityHint.retrieveValue(inputs[7]).get(0).doubleValue();
        final double maxSolidity = solidityHint.retrieveValue(inputs[7]).get(1).doubleValue();
        final double minVertexCount = minVertexHint.retrieveValue(inputs[8]).doubleValue();
        final double maxVertexCount = maxVertexHint.retrieveValue(inputs[9]).doubleValue();
        final double minRatio = minRatioHint.retrieveValue(inputs[10]).doubleValue();
        final double maxRatio = maxRatioHint.retrieveValue(inputs[11]).doubleValue();

        final ContoursReport inputContoursReport = contoursHint.retrieveValue(inputSocket);
        final MatVector inputContours = inputContoursReport.getContours();
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
            final double solidity = 100d * area / contourArea(hull);
            if (solidity < minSolidity || solidity > maxSolidity) continue;

            if(contour.rows() < minVertexCount || contour.rows() > maxVertexCount) continue;

            final double ratio = bb.width() / bb.height();
            if (ratio < minRatio || ratio > maxRatio) continue;

            outputContours.put(filteredContourCount++, contour);
        }

        outputContours.resize(filteredContourCount);

        final Socket<ContoursReport> outputSocket = contoursHint.safeCastSocket(outputs[0]);
        outputSocket.setValue(new ContoursReport(outputContours,
                inputContoursReport.getRows(), inputContoursReport.getCols()));
    }
}
