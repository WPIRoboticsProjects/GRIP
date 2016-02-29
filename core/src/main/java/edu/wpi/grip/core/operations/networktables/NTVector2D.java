package edu.wpi.grip.core.operations.networktables;

import java.util.function.Function;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * A type publishable to NetworkTables that consists of two numbers.  JavaCV {@link Point}s and {@link Size}s are
 * converted into this.
 *
 * @see NTPublishOperation#NTPublishOperation(Class, Class, Function)
 */
public class NTVector2D implements NTPublishable {

    private final double x, y;

    public NTVector2D(Point point) {
        this.x = point.x();
        this.y = point.y();
    }

    public NTVector2D(Size size) {
        this.x = size.width();
        this.y = size.height();
    }

    @NTValue(key = "x", weight = 0)
    public double getX() {
        return x;
    }

    @NTValue(key = "y", weight = 1)
    public double getY() {
        return y;
    }
}
