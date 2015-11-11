package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;

import java.io.InputStream;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.bitwise_xor;

/**
 * An {@link Operation} that masks out an area of interest from an image
 */
public class MaskOperation implements Operation {

    private final SocketHint<Mat> inputHint = new SocketHint<Mat>("Input", Mat.class, Mat::new);
    private final SocketHint<Mat> maskHint = new SocketHint<Mat>("Mask", Mat.class, Mat::new);

    private final SocketHint<Mat> outputHint = new SocketHint<Mat>("Output", Mat.class, Mat::new);

    @Override
    public String getName() {
        return "Mask";
    }

    @Override
    public String getDescription() {
        return "Filter out an area of interest in an image using a binary mask";
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
        final Mat input = ((InputSocket<Mat>) inputs[0]).getValue();
        final Mat mask = ((InputSocket<Mat>) inputs[1]).getValue();

        final OutputSocket<Mat> outputSocket = (OutputSocket<Mat>) outputs[0];
        final Mat output = outputSocket.getValue();

        // Do nothing if nothing is connected to the input
        // TODO: this should happen automatically for all sockets that are marked as required
        if (input.empty()) {
            outputSocket.setValue(outputSocket.getSocketHint().createInitialValue());
            return;
        }

        // Clear the output to black, then copy the input to it with the mask
        bitwise_xor(output, output, output);
        input.copyTo(output, mask);
        outputSocket.setValue(output);
    }
}
