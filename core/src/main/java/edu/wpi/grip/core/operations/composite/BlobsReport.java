package edu.wpi.grip.core.operations.composite;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.operations.network.PublishValue;
import edu.wpi.grip.core.operations.network.Publishable;
import edu.wpi.grip.core.sockets.NoSocketTypeLabel;

import java.util.Collections;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * This class is used as the output of operations that detect blobs in an image
 */
@NoSocketTypeLabel
public class BlobsReport implements Publishable {
    private final Mat input;
    private final List<Blob> blobs;

    public static class Blob {
        public final double x, y, size;

        protected Blob(double x, double y, double size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("x", x)
                    .add("y", y)
                    .add("size", size)
                    .toString();
        }
    }

    /**
     * Create an empty blob report.  This is used as the default value for sockets
     */
    public BlobsReport() {
        this(new Mat(), Collections.emptyList());
    }

    public BlobsReport(Mat input, List<Blob> blobs) {
        this.input = input;
        this.blobs = blobs;
    }

    public List<Blob> getBlobs() {
        return Collections.unmodifiableList(this.blobs);
    }

    /**
     * @return The original image that the blob detection was performed on
     */
    public Mat getInput() {
        return this.input;
    }

    @PublishValue(key = "x", weight = 0)
    public double[] getX() {
        final double[] x = new double[blobs.size()];
        for (int i = 0; i < blobs.size(); i++) {
            x[i] = blobs.get(i).x;
        }
        return x;
    }

    @PublishValue(key = "y", weight = 1)
    public double[] getY() {
        final double[] y = new double[blobs.size()];
        for (int i = 0; i < blobs.size(); i++) {
            y[i] = blobs.get(i).y;
        }
        return y;
    }

    @PublishValue(key = "size", weight = 2)
    public double[] getSize() {
        final double[] sizes = new double[blobs.size()];
        for (int i = 0; i < blobs.size(); i++) {
            sizes[i] = blobs.get(i).size;
        }
        return sizes;
    }

    @Override
    public String toString() {
        return blobs.toString();
    }

}
