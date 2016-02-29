package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;

import java.io.InputStream;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * An {@link Operation} that converts a color image into shades of gray
 */
public class DesaturateOperation implements Operation {

    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");

    @Override
    public String getName() {
        return "Desaturate";
    }

    @Override
    public String getDescription() {
        return "Convert a color image into shades of gray.";
    }

    @Override
    public Category getCategory() {
        return Category.IMAGE_PROCESSING;
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/desaturate.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{new InputSocket<>(eventBus, inputHint)};
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{new OutputSocket<>(eventBus, outputHint)};
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat input = ((InputSocket<Mat>) inputs[0]).getValue().get();

        final OutputSocket<Mat> outputSocket = (OutputSocket<Mat>) outputs[0];
        Mat output = outputSocket.getValue().get();


        switch (input.channels()) {
            case 1:
                // If the input is already one channel, it's already desaturated
                input.copyTo(output);
                break;

            case 3:
                cvtColor(input, output, COLOR_BGR2GRAY);
                break;

            case 4:
                cvtColor(input, output, COLOR_BGRA2GRAY);
                break;

            default:
                throw new IllegalArgumentException("Input to desaturate must have 1, 3, or 4 channels");
        }

        outputSocket.setValue(output);
    }
}
