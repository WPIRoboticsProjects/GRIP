package edu.wpi.grip.core.operations.composite;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * This class is used as the output of operations that detect blobs in an image
 */
public class BlobsReport {
    private Mat input = new Mat();
    private List<Blob> blobs = new ArrayList<>();

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

    @Override
    public String toString() {
        return this.blobs.stream().map(Blob::toString).collect(Collectors.joining("\n"));
    }

    public void setBlobs(List<Blob> blobs) {
        this.blobs = blobs;
    }

    public List<Blob> getBlobs() {
        return Collections.unmodifiableList(this.blobs);
    }

    public void setInput(Mat input) {
        this.input = input;
    }

    /**
     * @return The original image that the blob detection was performed on
     */
    public Mat getInput() {
        return this.input;
    }
}
