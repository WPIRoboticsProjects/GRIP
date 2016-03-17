
package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;

import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.SocketHints;

import java.io.InputStream;
import java.util.Optional;

import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.*;

/**
 * GRIP {@link Operation} for
 * {@link org.bytedeco.javacpp.opencv_core#normalize}.
 *
 * @author Sam Carlberg
 */
public class NormalizeOperation implements Operation {

    private enum Type {

        NORM_INF("NORM_INF", opencv_core.NORM_INF),
        NORM_L1("NORM_L1", opencv_core.NORM_L1),
        NORM_L2("NORM_L2", opencv_core.NORM_L2),
        NORM_MINMAX("NORM_MINMAX", opencv_core.NORM_MINMAX);

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
    private final SocketHint<Type> typeHint = SocketHints.createEnumSocketHint("Type", Type.NORM_MINMAX);
    private final SocketHint<Number> aHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Alpha", 0.0, 0, Double.MAX_VALUE);
    private final SocketHint<Number> bHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Beta", 1.0, 0, Double.MAX_VALUE);

    private final SocketHint<Mat> dstHint = SocketHints.Inputs.createMatSocketHint("Output", true);

    @Override
    public String getName() {
        return "Normalize";
    }

    @Override
    public String getDescription() {
        return "Normalizes an image";
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
        final Mat input = (Mat) inputs[0].getValue().get();
        final Type type = (Type) inputs[1].getValue().get();
        final Number a = (Number) inputs[2].getValue().get();
        final Number b = (Number) inputs[3].getValue().get();

        final OutputSocket<Mat> outputSocket = (OutputSocket<Mat>) outputs[0];
        final Mat output = outputSocket.getValue().get();

        normalize(input, output, a.doubleValue(), b.doubleValue(), type.value, -1, null);

        outputSocket.setValue(output);
    }

}
