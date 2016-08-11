package edu.wpi.grip.ui.codegeneration.tools;

import org.opencv.core.Point;

public class PyLine extends TestLine {
  private final double x1;
  private final double y1;
  private final double x2;
  private final double y2;

  public PyLine(double x1, double y1, double x2, double y2) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  }

  @Override
  public double getLength() {
    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
  }

  @Override
  public double getAngle() {
    return Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
  }

  @Override
  public Point getPoint1() {
    return new Point(x1, y1);
  }

  @Override
  public Point getPoint2() {
    return new Point(x2, y2);
  }
}
