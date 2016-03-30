package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.*;
import edu.wpi.grip.core.Operation;

import java.io.InputStream;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

/**
 * Scale an image to an exact width and height using one of several interpolation modes.  Scaling images down can
 * be a useful optimization, and scaling them up might be necessary for combining multiple images that are different
 * sizes.
 */
public class ResizeOperation implements Operation {
    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<Number> widthHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Width", 640);
    private final SocketHint<Number> heightHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Height", 480);
    private final SocketHint<Interpolation> interpolationHint = SocketHints.createEnumSocketHint("Interpolation", Interpolation.CUBIC);
    private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");

    private enum Interpolation {
        NEAREST("None", INTER_NEAREST),
        LINEAR("Linear", INTER_LINEAR),
        CUBIC("Cubic", INTER_CUBIC),
        LANCZOS("Lanczos", INTER_LANCZOS4),
        AREA("Area", INTER_AREA);

        final String label;
        final int value;

        Interpolation(String label, int value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @Override
    public String getName() {
        return "Resize Image";
    }

    @Override
    public String getDescription() {
        return "Scale an image to an exact size";
    }

    @Override
    public Category getCategory() {
        return Category.IMAGE_PROCESSING;
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/resize.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, inputHint),
                new InputSocket<>(eventBus, widthHint),
                new InputSocket<>(eventBus, heightHint),
                new InputSocket<>(eventBus, interpolationHint),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{
                new OutputSocket<>(eventBus, outputHint),
        };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat input = inputHint.retrieveValue(inputs[0]);
        final Number width = widthHint.retrieveValue(inputs[1]);
        final Number height = heightHint.retrieveValue(inputs[2]);
        final Interpolation interpolation = interpolationHint.retrieveValue(inputs[3]);

        final Socket<Mat> outputSocket = outputHint.safeCastSocket(outputs[0]);
        final Mat output = outputHint.retrieveValue(outputSocket);

        resize(input, output, new Size(width.intValue(), height.intValue()), 0.0, 0.0, interpolation.value);

        outputSocket.setValue(output);
    }
}
