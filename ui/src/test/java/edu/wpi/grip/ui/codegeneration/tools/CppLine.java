package edu.wpi.grip.ui.codegeneration.tools;

import org.opencv.core.Point;

public class CppLine extends TestLine {

  private final double x1;
  private final double y1;
  private final double x2;
  private final double y2;
  private final double length;
  private final double angle;

  /**
   * Creates a line from the parameters gotten from array. Note this takes length and angle as
   * parameters to ensure that generated code calculates length and angle properly.
   *
   * @param x1     X location of point 1.
   * @param y1     Y location of point 1.
   * @param x2     X location of point 2.
   * @param y2     Y location of point 2.
   * @param length length of the line.
   * @param angle  angle of the line.
   */
  public CppLine(double x1, double y1, double x2,
                 double y2, double length, double angle) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.length = length;
    this.angle = angle;
  }

  @Override
  public double getLength() {
    return length;
  }

  @Override
  public double getAngle() {
    return angle;
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
