package edu.wpi.grip.core.operations.composite;

import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import java.util.List;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_imgproc.arcLength;
import static org.bytedeco.javacpp.opencv_imgproc.boundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.contourArea;
import static org.bytedeco.javacpp.opencv_imgproc.convexHull;

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

    public static final OperationDescription DESCRIPTION =
            OperationDescription.builder()
                    .name("Filter Contours")
                    .summary("Find contours matching certain criteria")
                    .category(OperationDescription.Category.FEATURE_DETECTION)
                    .icon(Icon.iconStream("find-contours"))
                    .build();

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


    private final InputSocket<ContoursReport> contoursSocket;
    private final InputSocket<Number> minAreaSocket;
    private final InputSocket<Number> minPerimeterSocket;
    private final InputSocket<Number> minWidthSocket, maxWidthSocket;
    private final InputSocket<Number> minHeightSocket, maxHeightSocket;
    private final InputSocket<List<Number>> soliditySocket;
    private final InputSocket<Number> minVertexSocket, maxVertexSocket;
    private final InputSocket<Number> minRatioSocket, maxRatioSocket;

    private final OutputSocket<ContoursReport> outputSocket;

    public FilterContoursOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory outputSocketFactory) {
        this.contoursSocket = inputSocketFactory.create(contoursHint);
        this.minAreaSocket = inputSocketFactory.create(minAreaHint);
        this.minPerimeterSocket = inputSocketFactory.create(minPerimeterHint);
        this.minWidthSocket = inputSocketFactory.create(minWidthHint);
        this.maxWidthSocket = inputSocketFactory.create(maxWidthHint);
        this.minHeightSocket = inputSocketFactory.create(minHeightHint);
        this.maxHeightSocket = inputSocketFactory.create(maxHeightHint);
        this.soliditySocket = inputSocketFactory.create(solidityHint);
        this.minVertexSocket = inputSocketFactory.create(minVertexHint);
        this.maxVertexSocket = inputSocketFactory.create(maxVertexHint);
        this.minRatioSocket = inputSocketFactory.create(minRatioHint);
        this.maxRatioSocket = inputSocketFactory.create(maxRatioHint);

        this.outputSocket = outputSocketFactory.create(contoursHint);
    }

    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.of(
                contoursSocket,
                minAreaSocket,
                minPerimeterSocket,
                minWidthSocket,
                maxWidthSocket,
                minHeightSocket,
                maxHeightSocket,
                soliditySocket,
                maxVertexSocket,
                minVertexSocket,
                minRatioSocket,
                maxRatioSocket
        );
    }

    @Override
    public List<OutputSocket> getOutputSockets() {
        return ImmutableList.of(
                outputSocket
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform() {
        final InputSocket<ContoursReport> inputSocket = contoursSocket;
        final double minArea = minAreaSocket.getValue().get().doubleValue();
        final double minPerimeter = minPerimeterSocket.getValue().get().doubleValue();
        final double minWidth = minWidthSocket.getValue().get().doubleValue();
        final double maxWidth = maxWidthSocket.getValue().get().doubleValue();
        final double minHeight = minHeightSocket.getValue().get().doubleValue();
        final double maxHeight = maxHeightSocket.getValue().get().doubleValue();
        final double minSolidity = soliditySocket.getValue().get().get(0).doubleValue();
        final double maxSolidity = soliditySocket.getValue().get().get(1).doubleValue();
        final double minVertexCount = minVertexSocket.getValue().get().doubleValue();
        final double maxVertexCount = maxVertexSocket.getValue().get().doubleValue();
        final double minRatio = minRatioSocket.getValue().get().doubleValue();
        final double maxRatio = maxRatioSocket.getValue().get().doubleValue();


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

            if (contour.rows() < minVertexCount || contour.rows() > maxVertexCount) continue;

            final double ratio = bb.width() / bb.height();
            if (ratio < minRatio || ratio > maxRatio) continue;

            outputContours.put(filteredContourCount++, contour);
        }

        outputContours.resize(filteredContourCount);

        outputSocket.setValue(new ContoursReport(outputContours,
                inputSocket.getValue().get().getRows(), inputSocket.getValue().get().getCols()));
    }
}
