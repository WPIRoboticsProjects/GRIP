package edu.wpi.grip.ui.codegeneration.tools;

import org.opencv.core.Point;

import java.util.List;

public abstract class TestLine {
  public static final double pointTol = 2.0;
  public static final double lengthTol = 2.0;
  public static final double angleTol = 5.0;

  public abstract double getLength();

  public abstract double getAngle();

  public abstract Point getPoint1();

  public abstract Point getPoint2();

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

  public static boolean containsLin(TestLine line, List<TestLine> lines) {
    for (int i = 0; i < lines.size(); i++) {
      if ((lines.get(i).getPoint1().x - line.getPoint1().x) <= 2
          && (lines.get(i).getPoint1().y - line.getPoint1().y) <= 2
          && (lines.get(i).getPoint2().x - line.getPoint2().x) <= 2
          && (lines.get(i).getPoint2().y - line.getPoint2().y) <= 2) {
        return true;
      }
    }
    return false;
  }

  private static String errorMessage(TestLine grip, TestLine gen) {
    StringBuilder bld = new StringBuilder(22);
    bld.append("Grip was:").append(grip.toString())
        .append("\nGen was:").append(gen.toString()).append('\n');
    return bld.toString();
  }

  private static double angleDif(double alpha, double beta) {
    double diff = Math.abs(alpha - beta) % 360.0;
    diff = diff > 180 ? 360.0 - diff : diff;
    return diff;
  }
}
