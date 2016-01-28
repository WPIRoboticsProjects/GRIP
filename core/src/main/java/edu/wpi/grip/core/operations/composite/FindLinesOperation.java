package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import org.bytedeco.javacpp.indexer.FloatIndexer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Find line segments in a color or grayscale image
 */
public class FindLinesOperation implements Operation {

    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<LinesReport> linesHint = new SocketHint.Builder<>(LinesReport.class)
            .identifier("Lines").initialValueSupplier(LinesReport::new).build();

    @Override
    public String getName() {
        return "Find Lines";
    }

    @Override
    public String getDescription() {
        return "Detect line segments in an image.";
    }

    @Override
    public Category getCategory() {
        return Category.FEATURE_DETECTION;
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/find-lines.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{new InputSocket<>(eventBus, inputHint)};
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{new OutputSocket<>(eventBus, linesHint)};
    }

    @Override
    public Optional<Mat> createData() {
        return Optional.of(new Mat());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        final Mat input = (Mat) inputs[0].getValue().get();
        final OutputSocket<LinesReport> linesReportSocket = (OutputSocket<LinesReport>) outputs[0];
        final LineSegmentDetector lsd = linesReportSocket.getValue().get().getLineSegmentDetector();

        final Mat lines = new Mat();
        if (input.channels() == 1) {
            lsd.detect(input, lines);
        } else {
            // The line detector works on a single channel.  If the input is a color image, we can just give the line
            // detector a grayscale version of it
            final Mat tmp = (Mat) data.get();
            cvtColor(input, tmp, COLOR_BGR2GRAY);
            lsd.detect(tmp, lines);
        }

        // Store the lines in the LinesReport object
        List<LinesReport.Line> lineList = new ArrayList<>();
        if (!lines.empty()) {
            final FloatIndexer indexer = lines.<FloatIndexer>createIndexer();
            final float[] tmp = new float[4];
            for (int i = 0; i < lines.rows(); i++) {
                indexer.get(i, tmp);
                lineList.add(new LinesReport.Line(tmp[0], tmp[1], tmp[2], tmp[3]));
            }
        }

        linesReportSocket.setValue(new LinesReport(lsd, input, lineList));
    }
}
