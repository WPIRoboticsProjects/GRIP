package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.NoSocketTypeLabel;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgproc.LineSegmentDetector;
import static org.bytedeco.javacpp.opencv_imgproc.createLineSegmentDetector;

/**
 * This class contains the results of a line detection algorithm.  It has an input matrix (the image supplied to
 * the algorithm), and and output matrix, which contains every line found in its rows.
 * <p>
 * This is used by FindLinesOperation as the type of its output socket, allowing other classes (like GUI previews
 * and line filtering operations) to have a type-safe way of operating on line detection results and not just any
 * random matrix.
 */
@NoSocketTypeLabel
public class LinesReport {
    private Mat input = new Mat();
    private List<Line> lines = new ArrayList<>();

    private final LineSegmentDetector lsd = createLineSegmentDetector();

    public static class Line {
        public final double x1, y1, x2, y2;

        public Line(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public double lengthSquared() {
            return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
        }
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public List<Line> getLines() {
        return this.lines;
    }

    public void setInput(Mat input) {
        this.input = input;
    }

    protected LineSegmentDetector getLineSegmentDetector() {
        return lsd;
    }

    /**
     * @return The original image that the line detection was performed on
     */
    public Mat getInput() {
        return this.input;
    }
}