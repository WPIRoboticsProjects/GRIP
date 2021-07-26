package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.global.opencv_imgproc;

public enum ConnectedComponentsTypesEnum {

    /** The leftmost (x) coordinate which is the inclusive start of the bounding
 *  box in the horizontal direction. */
    CC_STAT_LEFT(opencv_imgproc.CC_STAT_LEFT), /** The topmost (y) coordinate which is the inclusive start of the bounding
 *  box in the vertical direction. */
    CC_STAT_TOP(opencv_imgproc.CC_STAT_TOP), /** The horizontal size of the bounding box */
    CC_STAT_WIDTH(opencv_imgproc.CC_STAT_WIDTH), /** The vertical size of the bounding box */
    CC_STAT_HEIGHT(opencv_imgproc.CC_STAT_HEIGHT), /** The total area (in pixels) of the connected component */
    CC_STAT_AREA(opencv_imgproc.CC_STAT_AREA), CC_STAT_MAX(opencv_imgproc.CC_STAT_MAX);

    public final int value;

    ConnectedComponentsTypesEnum(int value) {
        this.value = value;
    }
}
