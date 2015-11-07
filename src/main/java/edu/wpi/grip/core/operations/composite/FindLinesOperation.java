package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.indexer.FloatIndexer;

import java.io.InputStream;
import java.util.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Find line segments in a color or grayscale image
 */
public class FindLinesOperation implements Operation {

    private final SocketHint<Mat> inputHint = new SocketHint<Mat>("Input", Mat.class, Mat::new);
    private final SocketHint<LinesReport> linesHint = new SocketHint<LinesReport>("Lines", LinesReport.class,
            LinesReport::new);

    @Override
    public String getName() {
        return "Find Lines";
    }

    @Override
    public String getDescription() {
        return "Detect line segments in an image";
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

    private final Mat tmp = new Mat();

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat input = (Mat) inputs[0].getValue();
        final OutputSocket<LinesReport> linesReportSocket = (OutputSocket<LinesReport>) outputs[0];
        final LinesReport linesReport = linesReportSocket.getValue();

        // Do nothing if nothing is connected to the input
        // TODO: report what preconditions (like a non-empty matrix) are necessary for operations
        if (input.empty()) {
            return;
        }

        final Mat lines = new Mat();

        if (input.channels() == 1) {
            linesReport.getLineSegmentDetector().detect(input, lines);
        } else {
            // The line detector works on a single channel.  If the input is a color image, we can just give the line
            // detector a grayscale version of it
            synchronized (this.tmp) {
                cvtColor(input, tmp, COLOR_BGR2GRAY);
                linesReport.getLineSegmentDetector().detect(tmp, lines);
            }
        }

        // Store the lines in the LinesReport object
        linesReport.setLines(new ArrayList<>());
        if (!lines.empty()) {
            final FloatIndexer indexer = lines.<FloatIndexer>createIndexer();
            final float[] tmp = new float[4];
            for (int i = 0; i < lines.rows(); i++) {
                indexer.get(i, tmp);
                linesReport.getLines().add(new LinesReport.Line(tmp[0], tmp[1], tmp[2], tmp[3]));
            }
        }

        linesReport.setInput(input);
        linesReportSocket.setValue(linesReport);
    }
}
