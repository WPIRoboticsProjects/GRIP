package edu.wpi.grip.core.sources;

import org.bytedeco.javacpp.opencv_core.Mat;

public interface CameraSourceUpdater {
    void setFrameRate(double value);
    void copyNewMat(Mat matToCopy);
    void updatesComplete();
}
