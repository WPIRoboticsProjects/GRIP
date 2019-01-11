package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.javacpp.opencv_imgproc;

public enum SubdivEnum {

    PTLOC_ERROR(opencv_imgproc.Subdiv2D.PTLOC_ERROR), PTLOC_OUTSIDE_RECT(opencv_imgproc.Subdiv2D.PTLOC_OUTSIDE_RECT), PTLOC_INSIDE(opencv_imgproc.Subdiv2D.PTLOC_INSIDE), PTLOC_VERTEX(opencv_imgproc.Subdiv2D.PTLOC_VERTEX), PTLOC_ON_EDGE(opencv_imgproc.Subdiv2D.PTLOC_ON_EDGE), NEXT_AROUND_ORG(opencv_imgproc.Subdiv2D.NEXT_AROUND_ORG), NEXT_AROUND_DST(opencv_imgproc.Subdiv2D.NEXT_AROUND_DST), PREV_AROUND_ORG(opencv_imgproc.Subdiv2D.PREV_AROUND_ORG), PREV_AROUND_DST(opencv_imgproc.Subdiv2D.PREV_AROUND_DST), NEXT_AROUND_LEFT(opencv_imgproc.Subdiv2D.NEXT_AROUND_LEFT), NEXT_AROUND_RIGHT(opencv_imgproc.Subdiv2D.NEXT_AROUND_RIGHT), PREV_AROUND_LEFT(opencv_imgproc.Subdiv2D.PREV_AROUND_LEFT), PREV_AROUND_RIGHT(opencv_imgproc.Subdiv2D.PREV_AROUND_RIGHT);

    public final int value;

    SubdivEnum(int value) {
        this.value = value;
    }
}
