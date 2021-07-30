package edu.wpi.grip.generated.opencv_imgproc.enumeration;

import org.bytedeco.opencv.opencv_imgproc.Subdiv2D;

public enum SubdivEnum {

  PTLOC_ERROR(Subdiv2D.PTLOC_ERROR), PTLOC_OUTSIDE_RECT(Subdiv2D.PTLOC_OUTSIDE_RECT), PTLOC_INSIDE(Subdiv2D.PTLOC_INSIDE), PTLOC_VERTEX(Subdiv2D.PTLOC_VERTEX), PTLOC_ON_EDGE(Subdiv2D.PTLOC_ON_EDGE), NEXT_AROUND_ORG(Subdiv2D.NEXT_AROUND_ORG), NEXT_AROUND_DST(Subdiv2D.NEXT_AROUND_DST), PREV_AROUND_ORG(Subdiv2D.PREV_AROUND_ORG), PREV_AROUND_DST(Subdiv2D.PREV_AROUND_DST), NEXT_AROUND_LEFT(Subdiv2D.NEXT_AROUND_LEFT), NEXT_AROUND_RIGHT(Subdiv2D.NEXT_AROUND_RIGHT), PREV_AROUND_LEFT(Subdiv2D.PREV_AROUND_LEFT), PREV_AROUND_RIGHT(Subdiv2D.PREV_AROUND_RIGHT);

  public final int value;

  SubdivEnum(int value) {
    this.value = value;
  }
}
