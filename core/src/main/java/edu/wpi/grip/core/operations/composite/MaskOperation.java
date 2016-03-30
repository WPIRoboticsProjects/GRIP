package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.sockets.*;

import java.io.InputStream;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.bitwise_xor;

/**
 * An {@link Operation} that masks out an area of interest from an image
 */
public class MaskOperation implements Operation {

    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<Mat> maskHint = SocketHints.Inputs.createMatSocketHint("Mask", false);

    private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");

    @Override
    public String getName() {
        return "Mask";
    }

    @Override
    public String getDescription() {
        return "Filter out an area of interest in an image using a binary mask.";
    }

    @Override
    public Category getCategory() {
        return Category.IMAGE_PROCESSING;
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/mask.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, inputHint),
                new InputSocket<>(eventBus, maskHint),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{
                new OutputSocket<>(eventBus, outputHint)
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat input = inputHint.retrieveValue(inputs[0]);
        final Mat mask = maskHint.retrieveValue(inputs[1]);

        final Socket<Mat> outputSocket = outputHint.safeCastSocket(outputs[0]);
        final Mat output = outputHint.retrieveValue(outputSocket);

        // Clear the output to black, then copy the input to it with the mask
        bitwise_xor(output, output, output);
        input.copyTo(output, mask);
        outputSocket.setValue(output);
    }
}
