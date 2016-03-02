package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;

import java.io.InputStream;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * An {@link Operation} that softens an image using one of several different filters
 */
public class BlurOperation implements Operation {

    private enum Type {
        BOX("Box Blur"), GAUSSIAN("Gaussian Blur"), MEDIAN("Median Filter"), BILATERAL_FILTER("Bilateral Filter");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<Type> typeHint = SocketHints.createEnumSocketHint("Type", Type.BOX);
    private final SocketHint<Number> radiusHint = SocketHints.Inputs.createNumberSliderSocketHint("Radius", 0.0, 0.0, 100.0);

    private final SocketHint<Mat> outputHint = SocketHints.Inputs.createMatSocketHint("Output", true);

    @Override
    public String getName() {
        return "Blur";
    }

    @Override
    public String getDescription() {
        return "Soften the details of an image to remove noise.";
    }

    @Override
    public Category getCategory() {
        return Category.IMAGE_PROCESSING;
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/blur.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, inputHint),
                new InputSocket<>(eventBus, typeHint),
                new InputSocket<>(eventBus, radiusHint),
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
        final Mat input = ((InputSocket<Mat>) inputs[0]).getValue().get();
        final Type type = ((InputSocket<Type>) inputs[1]).getValue().get();
        final Number radius = ((InputSocket<Number>) inputs[2]).getValue().get();

        final OutputSocket<Mat> outputSocket = (OutputSocket<Mat>) outputs[0];
        final Mat output = outputSocket.getValue().get();

        int kernelSize;

        switch (type) {
            case BOX:
                // Box filter kernels must have an odd size
                kernelSize = 2 * radius.intValue() + 1;
                blur(input, output, new Size(kernelSize, kernelSize));
                break;

            case GAUSSIAN:
                // A Gaussian blur radius is a standard deviation, so a kernel that extends three radii in either direction
                // from the center should account for 99.7% of the theoretical influence on each pixel.
                kernelSize = 6 * radius.intValue() + 1;
                GaussianBlur(input, output, new Size(kernelSize, kernelSize), radius.doubleValue());
                break;

            case MEDIAN:
                kernelSize = 2 * radius.intValue() + 1;
                medianBlur(input, output, kernelSize);
                break;

            case BILATERAL_FILTER:
                bilateralFilter(input, output, -1, radius.doubleValue(), radius.doubleValue());
                break;

            default:
                throw new IllegalArgumentException("Illegal blur type: " + type);
        }

        outputSocket.setValue(output);
    }
}
