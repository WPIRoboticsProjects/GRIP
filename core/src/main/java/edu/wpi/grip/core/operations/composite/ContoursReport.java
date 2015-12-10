package edu.wpi.grip.core.operations.composite;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * The output of {@link FindContoursOperation}.  This stores a list of contours (which is basically a list of points) in
 * OpenCV objects, as well as the width and height of the image that the contours are from, to give context to the
 * points.
 */
public final class ContoursReport {
    private int rows, cols;
    private MatVector contours = new MatVector();

    public ContoursReport() {
        this(new MatVector(), -1, -1);
    }

    public ContoursReport(MatVector contours, int rows, int cols) {
        this.contours = contours;
        this.rows = rows;
        this.cols = cols;
    }

    public void setContours(MatVector contours) {
        this.contours = contours;
    }

    public MatVector getContours() {
        return this.contours;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getRows() {
        return this.rows;
    }

    public int getCols() {
        return this.cols;
    }
}
