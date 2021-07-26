package edu.wpi.grip.ui.codegeneration.tools;

import org.opencv.core.Point;

import java.util.List;

public abstract class TestLine {
  public static final double pointTol = 2.0;
  public static final double lengthTol = 2.0;
  public static final double angleTol = 5.0;

  public static boolean containsLin(TestLine line, List<TestLine> lines) {
    for (TestLine line1 : lines) {
      if ((line1.getPoint1().x - line.getPoint1().x) <= 2
          && (line1.getPoint1().y - line.getPoint1().y) <= 2
          && (line1.getPoint2().x - line.getPoint2().x) <= 2
          && (line1.getPoint2().y - line.getPoint2().y) <= 2) {
        return true;
      }
    }
    return false;
  }

  public abstract double getLength();

  public abstract double getAngle();

  public abstract Point getPoint1();

  public abstract Point getPoint2();

  @Override
  public String toString() {
    StringBuilder bld = new StringBuilder(24);
    bld.append("P1: ").append(pointToStr(getPoint1())).append(" P2: ")
        .append(pointToStr(getPoint2())).append(" Length=").append(getLength())
        .append(" Angle=").append(getAngle());
    return bld.toString();
  }

  protected final String pointToStr(Point p) {
    return "(" + p.x + "," + p.y + ")";
  }

}
