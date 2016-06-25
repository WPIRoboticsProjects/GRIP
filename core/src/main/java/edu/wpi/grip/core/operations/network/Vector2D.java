package edu.wpi.grip.core.operations.network;


import javax.annotation.concurrent.Immutable;

import static org.bytedeco.javacpp.opencv_core.Point;
import static org.bytedeco.javacpp.opencv_core.Size;

/**
 * A type publishable to a NetworkPublisher that consists of two numbers.  JavaCV {@link Point}s and
 * {@link Size}s are converted into this.
 */
@Immutable
public final class Vector2D implements Publishable {

  private final double x;
  private final double y;

  public Vector2D(Point point) {
    this.x = point.x();
    this.y = point.y();
  }

  public Vector2D(Size size) {
    this.x = size.width();
    this.y = size.height();
  }

  @PublishValue(key = "x", weight = 0)
  public double getX() {
    return x;
  }

  @PublishValue(key = "y", weight = 1)
  public double getY() {
    return y;
  }
}
