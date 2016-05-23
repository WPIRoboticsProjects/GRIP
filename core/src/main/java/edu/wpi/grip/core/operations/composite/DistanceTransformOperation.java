
package edu.wpi.grip.core.operations.composite;

import com.google.common.collect.ImmutableList;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.util.List;

import static org.bytedeco.javacpp.opencv_core.CV_8U;
import static org.bytedeco.javacpp.opencv_imgproc.CV_DIST_C;
import static org.bytedeco.javacpp.opencv_imgproc.CV_DIST_L1;
import static org.bytedeco.javacpp.opencv_imgproc.CV_DIST_L2;
import static org.bytedeco.javacpp.opencv_imgproc.distanceTransform;

/**
 * GRIP {@link Operation} for
 * {@link org.bytedeco.javacpp.opencv_imgproc#distanceTransform}.
 */
public class DistanceTransformOperation implements Operation {

    public static final OperationDescription DESCRIPTION =
            OperationDescription.builder()
                    .name("Distance Transform")
                    .summary("Sets the values of pixels in a binary image to their distance to the nearest black pixel.")
                    .category(OperationDescription.Category.IMAGE_PROCESSING)
                    .icon(Icon.iconStream("opencv"))
                    .build();

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

    /**
     * Masks are either 0x0, 3x3, or 5x5
     */
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


    private final InputSocket<Mat> srcSocket;
    private final InputSocket<Type> typeSocket;
    private final InputSocket<MaskSize> maskSizeSocket;

    private final OutputSocket<Mat> outputSocket;

    public DistanceTransformOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory outputSocketFactory) {
        this.srcSocket = inputSocketFactory.create(srcHint);
        this.typeSocket = inputSocketFactory.create(typeHint);
        this.maskSizeSocket = inputSocketFactory.create(maskSizeHint);

        this.outputSocket = outputSocketFactory.create(outputHint);
    }


    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.of(
                srcSocket,
                typeSocket,
                maskSizeSocket
        );
    }

    @Override
    public List<OutputSocket> getOutputSockets() {
        return ImmutableList.of(
                outputSocket
        );
    }

    @Override
    public void perform() {
        final Mat input = srcSocket.getValue().get();

        if (input.type() != CV_8U) {
            throw new IllegalArgumentException("Distance transform only works on 8-bit binary images");
        }

        final Type type = typeSocket.getValue().get();
        final MaskSize maskSize = maskSizeSocket.getValue().get();

        final Mat output = outputSocket.getValue().get();

        distanceTransform(input, output, type.value, maskSize.value);
        output.convertTo(output, CV_8U);

        outputSocket.setValue(output);
    }

}
