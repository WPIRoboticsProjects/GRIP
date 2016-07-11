package edu.wpi.grip.ui.codegeneration.tools;

import edu.wpi.grip.core.operations.composite.LinesReport;
import edu.wpi.grip.core.operations.composite.LinesReport.Line;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class GripLine extends TestLine {

  private double x1;
  private double  y1;
  private double x2;
  private double y2;
  private double angle;
  private double length;

  public GripLine(Line l) {
    x1 = l.x1;
    y1 = l.y1;
    x2 = l.x2;
    y2 = l.y2;
    angle = l.angle();
    length = l.length();
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

  public static List<TestLine> convertReport(LinesReport rep) {
    List<Line> repLine = rep.getLines();
    ArrayList<TestLine> lines = new ArrayList<TestLine>(repLine.size());
    for (int idx = 0; idx < repLine.size(); idx++) {
      lines.add(idx, new GripLine(repLine.get(idx)));
    }
    return lines;
  }
}
