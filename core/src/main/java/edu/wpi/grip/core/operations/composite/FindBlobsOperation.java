package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_features2d.SimpleBlobDetector;

/**
 * Find groups of similar pixels in a color or grayscale image
 */
public class FindBlobsOperation implements Operation {

    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<Number> minAreaHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Min Area", 1);
    private final SocketHint<List> circularityHint = SocketHints.Inputs.createNumberListRangeSocketHint("Circularity", 0.0, 1.0);
    private final SocketHint<Boolean> colorHint = SocketHints.createBooleanSocketHint("Dark Blobs", false);

    private final SocketHint<BlobsReport> blobsHint = new SocketHint.Builder(BlobsReport.class)
            .identifier("Blobs")
            .initialValueSupplier(BlobsReport::new)
            .build();

    @Override
    public String getName() {
        return "Find Blobs";
    }

    @Override
    public String getDescription() {
        return "Detect groups of pixels in an image.";
    }

    @Override
    public Category getCategory() {
        return Category.FEATURE_DETECTION;
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
        final Mat input = (Mat) inputs[0].getValue().get();
        final Number minArea = (Number) inputs[1].getValue().get();
        final List<Number> circularity = (List<Number>) inputs[2].getValue().get();
        final Boolean darkBlobs = (Boolean) inputs[3].getValue().get();


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

        ((OutputSocket<BlobsReport>) outputs[0]).setValue(new BlobsReport(input, blobs));
    }
}
