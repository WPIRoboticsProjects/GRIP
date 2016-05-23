
package edu.wpi.grip.core.operations.composite;

import com.google.common.collect.ImmutableList;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import java.util.List;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.NORM_INF;
import static org.bytedeco.javacpp.opencv_core.NORM_L1;
import static org.bytedeco.javacpp.opencv_core.NORM_L2;
import static org.bytedeco.javacpp.opencv_core.NORM_MINMAX;
import static org.bytedeco.javacpp.opencv_core.normalize;

/**
 * GRIP {@link Operation} for
 * {@link org.bytedeco.javacpp.opencv_core#normalize}.
 */
public class NormalizeOperation implements Operation {

    public static final OperationDescription DESCRIPTION =
            OperationDescription.builder()
                    .name("Normalize")
                    .summary("Normalizes or remaps the values of pixels in an image.")
                    .category(OperationDescription.Category.IMAGE_PROCESSING)
                    .icon(Icon.iconStream("opencv"))
                    .build();

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


    private final InputSocket<Mat> srcSocket;
    private final InputSocket<Type> typeSocket;
    private final InputSocket<Number> alphaSocket;
    private final InputSocket<Number> betaSocket;

    private final OutputSocket<Mat> outputSocket;

    public NormalizeOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory outputSocketFactory) {
        this.srcSocket = inputSocketFactory.create(srcHint);
        this.typeSocket = inputSocketFactory.create(typeHint);
        this.alphaSocket = inputSocketFactory.create(aHint);
        this.betaSocket = inputSocketFactory.create(bHint);

        this.outputSocket = outputSocketFactory.create(dstHint);
    }

    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.of(
                srcSocket,
                typeSocket,
                alphaSocket,
                betaSocket
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
        final Type type = typeSocket.getValue().get();
        final Number a = alphaSocket.getValue().get();
        final Number b = betaSocket.getValue().get();

        final Mat output = outputSocket.getValue().get();

        normalize(input, output, a.doubleValue(), b.doubleValue(), type.value, -1, null);

        outputSocket.setValue(output);
    }

}
