
package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;

import edu.wpi.grip.core.sockets.*;
import edu.wpi.grip.core.Operation;

import java.io.InputStream;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * GRIP {@link Operation} for
 * {@link org.bytedeco.javacpp.opencv_core#normalize}.
 */
public class NormalizeOperation implements Operation {

    private enum Type {

        INF("NORM_INF", NORM_INF),
        L1("NORM_L1", NORM_L1),
        L2("NORM_L2", NORM_L2),
        MINMAX("NORM_MINMAX", NORM_MINMAX);

        private final String label;
        private final int value;

        private Type(String name, int value) {
            this.label = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }

    }

    private final SocketHint<Mat> srcHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<Type> typeHint = SocketHints.createEnumSocketHint("Type", Type.MINMAX);
    private final SocketHint<Number> aHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Alpha", 0.0, 0, Double.MAX_VALUE);
    private final SocketHint<Number> bHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Beta", 255, 0, Double.MAX_VALUE);

    private final SocketHint<Mat> dstHint = SocketHints.Inputs.createMatSocketHint("Output", true);

    @Override
    public String getName() {
        return "Normalize";
    }

    @Override
    public String getDescription() {
        return "Normalizes or remaps the pixel values in an image.";
    }

    @Override
    public Category getCategory() {
        return Category.IMAGE_PROCESSING;
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/opencv.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
            new InputSocket<>(eventBus, srcHint),
            new InputSocket<>(eventBus, typeHint),
            new InputSocket<>(eventBus, aHint),
            new InputSocket<>(eventBus, bHint)
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[]{
            new OutputSocket<>(eventBus, dstHint)
        };
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        final Mat input = srcHint.retrieveValue(inputs[0]);
        final Type type = typeHint.retrieveValue(inputs[1]);
        final Number a = aHint.retrieveValue(inputs[2]);
        final Number b = bHint.retrieveValue(inputs[3]);

        final Socket<Mat> outputSocket = dstHint.safeCastSocket(outputs[0]);
        final Mat output = dstHint.retrieveValue(outputSocket);

        normalize(input, output, a.doubleValue(), b.doubleValue(), type.value, -1, null);

        outputSocket.setValue(output);
    }

}
