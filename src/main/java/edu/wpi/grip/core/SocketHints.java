package edu.wpi.grip.core;


import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;


/**
 * Create default socket hints
 */
public final class SocketHints {

    private SocketHints() { /* no op */ }

    public static final class Outputs {
        private Outputs() { /* no op */ }

        public static SocketHint<Mat> createMatSocketHint(final String identifier) {
            return SocketHints.createMatSocketHint(identifier, true);
        }

        public static SocketHint<Point> createPointSocketHint(final String identifier) {
            return SocketHints.createPointSocketHint(identifier, true);
        }

        public static SocketHint<Size> createSizeSocketHint(final String identifier) {
            return SocketHints.createSizeSocketHint(identifier, true);
        }

        public static SocketHint<Boolean> createBooleanSocketHint(final String identifier, boolean defaultValue) {
            return new SocketHint.Builder(Boolean.class).identifier(identifier).initialValue(defaultValue).publishable(true).build();
        }

        public static SocketHint<Number> createNumberSocketHint(final String identifier, Number defaultValue) {
            return new SocketHint.Builder(Number.class).identifier(identifier).initialValue(defaultValue).publishable(true).build();
        }

    }

    public static SocketHint<Mat> createMatSocketHint(final String identifier, final boolean withDefault) {
        return createObjectSocketHintBuilder(identifier, Mat.class, Mat::new, withDefault).build();
    }

    public static SocketHint<Size> createSizeSocketHint(final String identifier, final boolean withDefault) {
        return createObjectSocketHintBuilder(identifier, Size.class, Size::new, withDefault).build();
    }


    public static SocketHint<Point> createPointSocketHint(final String identifier, final boolean withDefault) {
        return createObjectSocketHintBuilder(identifier, Point.class, Point::new, withDefault).build();
    }

    public static SocketHint<Number> createNumberSliderSocketHint(final String identifier, final Number number,
                                                            final Number[] domain) {
        return createNumberSocketHintBuilder(identifier, number).domain(domain).view(SocketHint.View.SLIDER).build();
    }

    public static SocketHint<Number> createNumberSpinnerSocketHint(final String identifier, final Number number,
                                                            final Number[] domain) {
        return createNumberSocketHintBuilder(identifier, number).domain(domain).view(SocketHint.View.SPINNER).build();
    }

    public static SocketHint<Number> createNumberSpinnerSocketHint(final String identifier, final Number number) {
        return createNumberSocketHintBuilder(identifier, number).view(SocketHint.View.SPINNER).build();
    }

    public static SocketHint<Number> createNumberSocketHint(final String identifier, final Number number) {
        return createNumberSocketHintBuilder(identifier, number).build();
    }

    public static SocketHint<Boolean> createBooleanSocketHint(final String identifier, final boolean initialValue) {
        return new SocketHint.Builder(Boolean.class)
                .identifier(identifier)
                .initialValue(initialValue)
                .view(SocketHint.View.CHECKBOX)
                .build();
    }

    public static SocketHint<List> createNumberListRangeSockeHint(final String identifier, final Number low, final Number high) {
        return new SocketHint.Builder(List.class).identifier(identifier)
                .initialValueSupplier(() -> Arrays.asList(low, high))
                .domain(new List[]{Arrays.asList(low, high)})
                .view(SocketHint.View.RANGE)
                .build();
    }

    private static <T> SocketHint.Builder<T> createObjectSocketHintBuilder(final String identifier,
                                                                   final Class<T> type,
                                                                   final Supplier<T> supplier,
                                                                   final boolean withDefault) {
        final SocketHint.Builder builder = new SocketHint.Builder(type).identifier(identifier);
        if (withDefault) return builder.initialValueSupplier(supplier);
        else return builder;
    }

    private static SocketHint.Builder<Number> createNumberSocketHintBuilder(final String identifier,
                                                                            final Number number) {
        return new SocketHint.Builder(Number.class).identifier(identifier).initialValue(number);
    }
}
