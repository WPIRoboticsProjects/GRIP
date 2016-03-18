
package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;

import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.SocketHints;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * GRIP {@link Operation} for
 * {@link org.bytedeco.javacpp.opencv_imgproc#watershed}.
 */
public class WatershedOperation implements Operation {

    private final SocketHint<Mat> srcHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<ContoursReport> contoursHint = new SocketHint.Builder<>(ContoursReport.class)
            .identifier("Contours")
            .initialValueSupplier(ContoursReport::new)
            .build();

    private final SocketHint<Mat> outputHint = SocketHints.Inputs.createMatSocketHint("Output", true);

    @Override
    public String getName() {
        return "Watershed";
    }

    @Override
    public String getDescription() {
        return "Isolates overlapping objects from the background and each other";
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
            new InputSocket<>(eventBus, srcHint),
            new InputSocket<>(eventBus, contoursHint)
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{
            new OutputSocket<>(eventBus, outputHint)
        };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat input = (Mat) inputs[0].getValue().get();
        if (input.type() != CV_8UC3) {
            throw new IllegalArgumentException("Watershed only works on 8-bit, 3-channel images");
        }

        final ContoursReport contourReport = (ContoursReport) inputs[1].getValue().get();
        final MatVector contours = contourReport.getContours();

        final OutputSocket<Mat> outputSocket = (OutputSocket<Mat>) outputs[0];
        final Mat markers = new Mat(input.size(), CV_32SC1, new Scalar(0.0));
        final Mat output = new Mat(markers.size(), CV_8UC1, new Scalar(0.0));

        // draw foreground markers (these have to be different colors)
        for (int i = 0; i < contours.size(); i++) {
            drawContours(markers, contours, i, Scalar.all((i + 1) * (255 / contours.size())), CV_FILLED, LINE_8, null, 2, null);
        }

        // draw background marker a different color from the foreground markers
        // TODO maybe make this configurable? There may be something in the corner
        circle(markers, new Point(5, 5), 3, Scalar.WHITE, -1, LINE_8, 0);

        watershed(input, markers);
        markers.convertTo(output, CV_8UC1);
        bitwise_not(output, output); // watershed inverts colors; invert them back

        outputSocket.setValue(output);

        // make sure that the working mat is freed to avoid a memory leak
        markers.release();
    }

}
