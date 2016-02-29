package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.NoSocketTypeLabel;
import edu.wpi.grip.core.operations.networktables.NTPublishable;
import edu.wpi.grip.core.operations.networktables.NTValue;

import java.util.Collections;
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
public class LinesReport implements NTPublishable {
    private final LineSegmentDetector lsd;
    private final Mat input;
    private final List<Line> lines;

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

        public double length() {
            return Math.sqrt(lengthSquared());
        }

        public double angle() {
            return Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
        }
    }

    /**
     * Construct an empty report.  This is used as a default value for {@link edu.wpi.grip.core.Socket}s containing
     * LinesReports.
     */
    public LinesReport() {
        this(createLineSegmentDetector(), new Mat(), Collections.emptyList());
    }

    public LinesReport(LineSegmentDetector lsd, Mat input, List<Line> lines) {
        this.lsd = lsd;
        this.input = input;
        this.lines = lines;
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

    public List<Line> getLines() {
        return this.lines;
    }

    @NTValue(key = "x1", weight = 0)
    public double[] getX1() {
        final double[] x1 = new double[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            x1[i] = lines.get(i).x1;
        }
        return x1;
    }

    @NTValue(key = "y1", weight = 1)
    public double[] getY1() {
        final double[] y1 = new double[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            y1[i] = lines.get(i).y1;
        }
        return y1;
    }

    @NTValue(key = "x2", weight = 2)
    public double[] getX2() {
        final double[] x2 = new double[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            x2[i] = lines.get(i).x2;
        }
        return x2;
    }

    @NTValue(key = "y2", weight = 3)
    public double[] getY2() {
        final double[] y2 = new double[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            y2[i] = lines.get(i).y2;
        }
        return y2;
    }

    @NTValue(key = "length", weight = 4)
    public double[] getLength() {
        final double[] length = new double[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            length[i] = lines.get(i).length();
        }
        return length;
    }

    @NTValue(key = "angle", weight = 5)
    public double[] getAngle() {
        final double[] angle = new double[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            angle[i] = lines.get(i).angle();
        }
        return angle;
    }
}