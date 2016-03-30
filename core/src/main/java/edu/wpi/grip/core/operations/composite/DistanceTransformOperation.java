
package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.*;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.InputStream;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.CV_8U;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * GRIP {@link Operation} for
 * {@link org.bytedeco.javacpp.opencv_imgproc#distanceTransform}.
 */
public class DistanceTransformOperation implements Operation {

    private enum Type {

        DIST_L1("CV_DIST_L1", CV_DIST_L1),
        DIST_L2("CV_DIST_L2", CV_DIST_L2),
        DIST_C("CV_DIST_C", CV_DIST_C);

        private final String label;
        private final int value;

        private Type(String label, int value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /** Masks are either 0x0, 3x3, or 5x5 */
    private enum MaskSize {

        ZERO("0x0", 0),
        THREE("3x3", 3),
        FIVE("5x5", 5);

        private final String label;
        private final int value;

        private MaskSize(String label, int value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final SocketHint<Mat> srcHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<Type> typeHint = SocketHints.createEnumSocketHint("Type", Type.DIST_L2);
    private final SocketHint<MaskSize> maskSizeHint = SocketHints.createEnumSocketHint("Mask size", MaskSize.ZERO);

    private final SocketHint<Mat> outputHint = SocketHints.Inputs.createMatSocketHint("Output", true);

    @Override
    public String getName() {
        return "Distance Transform";
    }

    @Override
    public String getDescription() {
        return "Sets the values of pixels in a binary image to their distance to the nearest black pixel.";
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
            new InputSocket<>(eventBus, maskSizeHint)
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
        final Mat input = srcHint.retrieveValue(inputs[0]);

        if (input.type() != CV_8U) {
            throw new IllegalArgumentException("Distance transform only works on 8-bit binary images");
        }

        final Type type = typeHint.retrieveValue(inputs[1]);
        final MaskSize maskSize = maskSizeHint.retrieveValue(inputs[2]);

        final Socket<Mat> outputSocket = outputHint.safeCastSocket(outputs[0]);
        final Mat output = outputHint.retrieveValue(outputSocket);

        distanceTransform(input, output, type.value, maskSize.value);
        output.convertTo(output, CV_8U);

        outputSocket.setValue(output);
    }

}
