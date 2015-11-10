package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_features2d.SimpleBlobDetector;

/**
 * Find groups of similar pixels in a color or grayscale image
 */
public class FindBlobsOperation implements Operation {

    private final SocketHint<Mat> inputHint = new SocketHint<Mat>("Input", Mat.class, Mat::new);
    private final SocketHint<Number> minAreaHint = new SocketHint<>("Min Area", Number.class, 1,
            SocketHint.View.SPINNER);
    private final SocketHint<List> circularityHint = new SocketHint<List>("Circularity", List.class,
            () -> Arrays.asList(0.0, 1.0), SocketHint.View.RANGE, new List[]{Arrays.asList(0, 1)});
    private final SocketHint<Boolean> colorHint = new SocketHint<>("Dark Blobs", Boolean.class, false,
            SocketHint.View.CHECKBOX);


    private final SocketHint<BlobsReport> blobsHint = new SocketHint<BlobsReport>("Blobs", BlobsReport.class,
            BlobsReport::new);

    @Override
    public String getName() {
        return "Find Blobs";
    }

    @Override
    public String getDescription() {
        return "Detect groups of pixels in an image";
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/find-blobs.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, inputHint),
                new InputSocket<>(eventBus, minAreaHint),
                new InputSocket<>(eventBus, circularityHint),
                new InputSocket<>(eventBus, colorHint),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{new OutputSocket<>(eventBus, blobsHint)};
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat input = (Mat) inputs[0].getValue();
        final Number minArea = (Number) inputs[1].getValue();
        final List<Number> circularity = (List<Number>) inputs[2].getValue();
        final Boolean darkBlobs = (Boolean) inputs[3].getValue();

        final OutputSocket<BlobsReport> blobsReportSocket = (OutputSocket<BlobsReport>) outputs[0];
        final BlobsReport blobsReport = blobsReportSocket.getValue();

        final List<BlobsReport.Blob> blobs = new ArrayList<>();

        blobsReport.setInput(input);
        blobsReport.setBlobs(blobs);

        // Do nothing if nothing is connected to the input
        // TODO: this should happen automatically for all sockets that are marked as required
        if (input.empty()) {
            blobsReportSocket.setValue(blobsReport);
            return;
        }

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

        for (int i = 0; i < keyPointVector.size(); i++) {
            final KeyPoint keyPoint = keyPointVector.get(i);
            blobs.add(new BlobsReport.Blob(keyPoint.pt().x(), keyPoint.pt().y(), keyPoint.size()));
        }

        blobsReportSocket.setValue(blobsReport);
    }
}
