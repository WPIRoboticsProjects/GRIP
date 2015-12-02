package edu.wpi.grip.core;


import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;


/**
 * Create default socket hints
 */
public final class SocketHints {

    private SocketHints() { /* no op */ }

    public static SocketHint<Mat> createMatSocketHint(final String identifier, final boolean withDefault) {
        final SocketHint.Builder builder = new SocketHint.Builder(Mat.class).identifier(identifier);
        if (withDefault) return builder.initialValueSupplier(Mat::new).build();
        else return builder.build();
    }


    public static SocketHint<Point> createPointSocketHint(final String identifier) {
        return new SocketHint.Builder(Point.class).identifier(identifier).initialValueSupplier(Point::new).build();
    }

    public static SocketHint<Number> createNumberSocketHint(final String identifier, final Number number,
                                                            final Number[] domain, final SocketHint.View view) {
        return createNumberSocketHintBuilder(identifier, number).domain(domain).view(view).build();
    }

    public static SocketHint<Number> createNumberSocketHint(final String identifier, final Number number,
                                                            final Number[] domain) {
        return createNumberSocketHintBuilder(identifier, number).domain(domain).build();
    }

    public static SocketHint<Number> createNumberSocketHint(final String identifier, final Number number) {
        return createNumberSocketHintBuilder(identifier, number).build();
    }

    public static SocketHint<Number> createNumberSocketHint(final String identifier, final Number number,
                                                            final boolean publishable) {
        return createNumberSocketHintBuilder(identifier, number).publishable(publishable).build();
    }

    private static SocketHint.Builder<Number> createNumberSocketHintBuilder(final String identifier,
                                                                            final Number number) {
        return new SocketHint.Builder(Number.class).identifier(identifier).initialValue(number);
    }
}
