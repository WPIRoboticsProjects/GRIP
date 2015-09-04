package edu.wpi.grip.core.operations.opencv;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Socket;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_core;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AddOperationTest {

    EventBus eventBus;
    Operation addition;

    @Before
    public void setUp() throws Exception {
        this.eventBus = new EventBus();
        this.addition = new AddOperation();
    }

    /**
     * This method was found here http://stackoverflow.com/a/32235744/3708426
     * Converted from C++ to Java
     *
     * @param mat1 The first Mat
     * @param mat2 The second Mat
     * @return true if the two Matrices are the same
     */
    private boolean isMatEqual(Mat mat1, Mat mat2) {
        // treat two empty mat as identical as well
        if (mat1.empty() && mat2.empty()) {
            return true;
        }
        // if dimensionality of two mat is not identical, these two mat is not identical
        if (mat1.cols() != mat2.cols() || mat1.rows() != mat2.rows() || mat1.dims() != mat2.dims()) {
            return false;
        }
        Mat diff = new Mat();
        opencv_core.compare(mat1, mat2, diff, opencv_core.CMP_NE);
        int nonZeroCount = opencv_core.countNonZero(diff);
        return nonZeroCount == 0;
    }

    @Test
    public void testAddMatrixOfOnesToMatrixOfTwosEqualsMatrixOfThrees() {
        // Given
        Socket[] inputs = addition.createInputSockets(eventBus);
        Socket[] outputs = addition.createOutputSockets(eventBus);
        Socket<Mat> term1 = inputs[0], term2 = inputs[1];
        Socket<Mat> sum = outputs[0];

        int dimentions[] = {100, 100, 100};

        term1.setValue(new Mat(3, dimentions, opencv_core.CV_8U, Scalar.all(1)));
        term2.setValue(new Mat(3, dimentions, opencv_core.CV_8U, Scalar.all(2)));

        //When
        addition.perform(inputs, outputs);

        //Then
        Mat expectedResult = new Mat(3, dimentions, opencv_core.CV_8U, Scalar.all(3));
        assertTrue(isMatEqual(sum.getValue(), expectedResult));
    }
}