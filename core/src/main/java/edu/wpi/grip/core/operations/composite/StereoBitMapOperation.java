package edu.wpi.grip.core.operations.composite;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import org.bytedeco.javacpp.opencv_calib3d.StereoBM;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.minMaxIdx;

public class StereoBitMapOperation implements Operation {
    public enum PreFilterType {
        NORMALIZED_RESPONSE(0),
        XSOBEL(1);
        final int value;
        PreFilterType(int value) {
            this.value = value;
        }
    }
    private final SocketHint<Mat>
            leftImageSocketHint = SocketHints.Inputs.createMatSocketHint("left", false),
            rightImageSocketHint = SocketHints.Inputs.createMatSocketHint("right", false);
    private final SocketHint<Number>
            numDisparitiesSocketHint = SocketHints.Inputs.createNumberSpinnerSocketHint("disparities", 0),
            blockSizeSocketHint = SocketHints.Inputs.createNumberSpinnerSocketHint("block size", 21);
    private final SocketHint<PreFilterType>
            filterTypeSocketHint = SocketHints.createEnumSocketHint("PreFilter Type", PreFilterType.NORMALIZED_RESPONSE);


    private final SocketHint<Mat>
            mappedDisparityImageSocketHint = SocketHints.Outputs.createMatSocketHint("Mapped Disparity");

    @Override
    public String getName() {
        return "Stereo Bit Map";
    }

    @Override
    public String getDescription() {
        return "Create depth map from stereo images";
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, leftImageSocketHint),
                new InputSocket<>(eventBus, rightImageSocketHint),
                new InputSocket<>(eventBus, numDisparitiesSocketHint),
                new InputSocket<>(eventBus, blockSizeSocketHint),
                new InputSocket<>(eventBus, filterTypeSocketHint)
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[] {
                new OutputSocket<>(eventBus, mappedDisparityImageSocketHint)
        };
    }

    @Override
    public Optional<?> createData(){
        return Optional.of(new Mat());
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        final Mat left = (Mat) inputs[0].getValue().get();
        final Mat right = (Mat) inputs[1].getValue().get();
        final int disparities = ((Number) inputs[2].getValue().get()).intValue();
        final int blockSize = ((Number) inputs[3].getValue().get()).intValue();
        final PreFilterType type = (PreFilterType) inputs[4].getValue().get();
        final Mat disparity = (Mat) data.get();
        final Mat mappedDisparity = (Mat) outputs[0].getValue().get();
        StereoBM matcher = null;
        try {
            matcher = StereoBM.create(disparities, blockSize);
            matcher.setPreFilterType(type.value);
            matcher.compute(left, right, disparity);
            // We have to convert the disparity map to an 8 bit unsigned single chanel image to use later on in GRIP
            final double min [] = new double[1];
            final double max [] = new double[1];
            minMaxIdx(disparity, min, max, null, null, null);
            disparity.convertTo(mappedDisparity, CV_8UC1, 255 / (max[0]-min[0]), -min[0]);
        } finally {
            try {
                matcher.close();
            } catch (Exception e) { // NO-PMD
                // We don't care about this exception.
            }
        }
    }

    @Override
    public void cleanUp(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        Mat dataMat = (Mat) data.get();
        try {
            dataMat.close();
        } catch (Exception e) { // NO-PMD
            // We don't care about this exception
        }
    }
}
