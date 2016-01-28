package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHints;

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
                new InputSocket<>(eventBus, SocketHints.Inputs.createMatSocketHint("Input", false)),
                new InputSocket<>(eventBus, SocketHints.Inputs.createNumberSpinnerSocketHint("Width", 640)),
                new InputSocket<>(eventBus, SocketHints.Inputs.createNumberSpinnerSocketHint("Height", 480)),
                new InputSocket<>(eventBus, SocketHints.createEnumSocketHint("Interpolation", Interpolation.CUBIC)),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{
                new OutputSocket<>(eventBus, SocketHints.Outputs.createMatSocketHint("Output")),
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat input = ((InputSocket<Mat>) inputs[0]).getValue().get();
        final Number width = ((InputSocket<Number>) inputs[1]).getValue().get();
        final Number height = ((InputSocket<Number>) inputs[2]).getValue().get();
        final Interpolation interpolation = ((InputSocket<Interpolation>) inputs[3]).getValue().get();

        final OutputSocket<Mat> outputSocket = (OutputSocket<Mat>) outputs[0];
        final Mat output = outputSocket.getValue().get();

        resize(input, output, new Size(width.intValue(), height.intValue()), 0.0, 0.0, interpolation.value);

        outputSocket.setValue(output);
    }
}
