package edu.wpi.grip.util;


import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class ImageWithData {
    private final int rows;
    private final int cols;
    public final File file;

    protected ImageWithData(File file, int rows, int cols) {
        this.file = file;
        this.rows = rows;
        this.cols = cols;
    }

    public void assertSameImage(final Mat image) {
        // Check that the image that is read in is 2 dimensional
        assertEquals("Matrix from loaded image did not have expected number of rows.", this.rows, image.rows());
        assertEquals("Matrix from loaded image did not have expected number of cols.", this.cols, image.cols());
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

}
