package edu.wpi.grip.ui.codegeneration.tools;

import org.opencv.core.Point;

import static org.junit.Assert.assertEquals;

public abstract class TestLine {
  public static final double pointTol = 2.0;
  public static final double lengthTol = 2.0;
  public static final double angleTol = 5.0;
  public abstract double getLength();
  public abstract double getAngle();
  public abstract Point getPoint1();
  public abstract Point getPoint2();

  public String toString(){
    StringBuilder bld = new StringBuilder();
    bld.append("P1: ").append(pointToStr(getPoint1()));
    bld.append(" P2: ").append(pointToStr(getPoint2()));
    bld.append(" Length=").append(getLength());
    bld.append(" Angle=").append(getAngle());
    return bld.toString();
  }
  protected final String pointToStr(Point p){
    return "(" + p.x + "," + p.y + ")";
  }
  
  public static void assertEqual(TestLine grip, TestLine gen){
    assertEquals("x1 coordinate not close\n" + errorMessage(grip, gen), 
        grip.getPoint1().x, gen.getPoint1().x, pointTol);
    assertEquals("y1 coordinate not close " + errorMessage(grip, gen),
        grip.getPoint1().y, gen.getPoint1().y, pointTol);
    assertEquals("x2 coordinate not close " + errorMessage(grip, gen), 
        grip.getPoint2().x, gen.getPoint2().x, pointTol);
    assertEquals("y2 coordinate not close " + errorMessage(grip, gen),
        grip.getPoint2().y, gen.getPoint2().y, pointTol);
    assertEquals("Length not close " + errorMessage(grip, gen),
        grip.getLength(), gen.getLength(), lengthTol);
    assertEquals("Angle not close " + errorMessage(grip, gen),
        0, angleDif(grip.getAngle(), gen.getAngle()), angleTol);
  }
  
  private static String errorMessage(TestLine grip, TestLine gen){
    StringBuilder bld = new StringBuilder();
    bld.append("Grip was:").append(grip.toString()).append("\n");
    bld.append("Gen was:").append(gen.toString()).append("\n");
    return bld.toString();
  }
  
  private static double angleDif(double alpha, double beta){
    double diff = Math.abs(alpha-beta)%360.0;
    diff = diff>180 ? 360.0 - diff : diff;
    return diff;
  }
}
