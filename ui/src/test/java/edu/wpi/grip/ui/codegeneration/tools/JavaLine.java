package edu.wpi.grip.ui.codegeneration.tools;

import org.opencv.core.Point;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.fail;

public class JavaLine extends TestLine {

  private final Object line;
  private static final Logger logger = Logger.getLogger(JavaLine.class.getName());

  public JavaLine(Object line) {
    this.line = line;
  }

  @Override
  public double getLength() {
    try {
      return (double) line.getClass().getMethod("length").invoke(line);
    } catch (NoSuchMethodException | SecurityException
        | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      fail("length is not valid for class " + line.getClass().getSimpleName());
      logger.log(Level.WARNING, e.getMessage(), e);
      return 0.0;
    }
  }

  @Override
  public double getAngle() {
    try {
      return (double) line.getClass().getMethod("angle").invoke(line);
    } catch (NoSuchMethodException | SecurityException
        | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      fail("angle is not valid for class " + line.getClass().getSimpleName());
      logger.log(Level.WARNING, e.getMessage(), e);
      return 0.0;
    }
  }

  @Override
  public Point getPoint1() {
    try {
      return new Point(line.getClass().getField("x1").getDouble(line),
          line.getClass().getField("y1").getDouble(line));
    } catch (NoSuchFieldException | SecurityException
        | IllegalAccessException | IllegalArgumentException e) {
      fail("angle is not valid for class " + line.getClass().getSimpleName());
      logger.log(Level.WARNING, e.getMessage(), e);
      return null;
    }
  }

  @Override
  public Point getPoint2() {
    try {
      return new Point(line.getClass().getField("x2").getDouble(line),
          line.getClass().getField("y2").getDouble(line));
    } catch (NoSuchFieldException | SecurityException
        | IllegalAccessException | IllegalArgumentException e) {
      fail("angle is not valid for class " + line.getClass().getSimpleName());
      logger.log(Level.WARNING, e.getMessage(), e);
      return null;
    }
  }

}
