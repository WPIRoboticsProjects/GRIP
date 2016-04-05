package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.Operation;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Scalar;

public abstract class ThresholdOperation<O extends ThresholdOperation<O>> implements Operation {

    protected Mat[] dataArray = {new Mat(), new Mat(), new Mat()};

    /**
     * @param dataArray The array with the element that should be re-allocated
     * @param index     The index of the data array that should be inspected
     * @param value     The value that should be assigned to the mat regardless of being reallocated
     * @param input     The input matrix that the dataArray element should be compared against
     * @return Either the old mat with the value assigned or a newly created Matrix.
     */
    protected Mat reallocateMatIfInputSizeOrWidthChanged(final Mat[] dataArray, final int index, final Scalar value, final Mat input) {
        if (dataArray[index].size().width() != input.size().width()
                || dataArray[index].size().height() != input.size().height()
                || dataArray[index].type() != input.type()) {
            return dataArray[index] = new Mat(input.size(), input.type(), value);
        } else {
            return dataArray[index].put(value);
        }
    }
}
