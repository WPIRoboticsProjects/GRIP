package edu.wpi.grip.core.operations.composite;

import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.KeyPoint;
import static org.bytedeco.javacpp.opencv_core.KeyPointVector;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_features2d.SimpleBlobDetector;

/**
 * Find groups of similar pixels in a color or grayscale image
 */
public class FindBlobsOperation implements Operation {

    public static final OperationDescription DESCRIPTION =
            OperationDescription.builder()
                    .name("Find Blobs")
                    .summary("Detects groups of pixels in an image.")
                    .category(OperationDescription.Category.FEATURE_DETECTION)
                    .icon(Icon.iconStream("find-blobs"))
                    .build();

    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<Number> minAreaHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Min Area", 1);
    private final SocketHint<List<Number>> circularityHint = SocketHints.Inputs.createNumberListRangeSocketHint("Circularity", 0.0, 1.0);
    private final SocketHint<Boolean> colorHint = SocketHints.createBooleanSocketHint("Dark Blobs", false);

    private final SocketHint<BlobsReport> blobsHint = new SocketHint.Builder<>(BlobsReport.class)
            .identifier("Blobs")
            .initialValueSupplier(BlobsReport::new)
            .build();

    private final InputSocket<Mat> inputSocket;
    private final InputSocket<Number> minAreaSocket;
    private final InputSocket<List<Number>> circularitySocket;
    private final InputSocket<Boolean> colorSocket;

    private final OutputSocket<BlobsReport> outputSocket;

    public FindBlobsOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory outputSocketFactory) {
        this.inputSocket = inputSocketFactory.create(inputHint);
        this.minAreaSocket = inputSocketFactory.create(minAreaHint);
        this.circularitySocket = inputSocketFactory.create(circularityHint);
        this.colorSocket = inputSocketFactory.create(colorHint);

        this.outputSocket = outputSocketFactory.create(blobsHint);
    }

    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.of(
                inputSocket,
                minAreaSocket,
                circularitySocket,
                colorSocket
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
        final Mat input = inputSocket.getValue().get();
        final Number minArea = minAreaSocket.getValue().get();
        final List<Number> circularity = circularitySocket.getValue().get();
        final Boolean darkBlobs = colorSocket.getValue().get();


        final SimpleBlobDetector blobDetector = SimpleBlobDetector.create(new SimpleBlobDetector.Params()
                .filterByArea(true)
                .minArea(minArea.intValue())
                .maxArea(Integer.MAX_VALUE)

                .filterByColor(true)
                .blobColor(darkBlobs ? (byte) 0 : (byte) 255)

                .filterByCircularity(true)
                .minCircularity(circularity.get(0).floatValue())
                .maxCircularity(circularity.get(1).floatValue()));

        // Detect the blobs and store them in the output BlobsReport
        final KeyPointVector keyPointVector = new KeyPointVector();
        blobDetector.detect(input, keyPointVector);

        final List<BlobsReport.Blob> blobs = new ArrayList<>();
        for (int i = 0; i < keyPointVector.size(); i++) {
            final KeyPoint keyPoint = keyPointVector.get(i);
            blobs.add(new BlobsReport.Blob(keyPoint.pt().x(), keyPoint.pt().y(), keyPoint.size()));
        }

        outputSocket.setValue(new BlobsReport(input, blobs));
    }
}
