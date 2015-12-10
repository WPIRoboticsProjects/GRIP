package edu.wpi.grip.core;


import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;


/**
 * Commonly used {@link SocketHint SocketHints}
 */
public final class SocketHints {

    /**
     * Provides some default domains
     */
    public enum Domain {
        INTEGERS(Integer.MIN_VALUE, Integer.MAX_VALUE),
        POSITIVE_INTEGERS(0d, Integer.MAX_VALUE),
        DOUBLES(-Double.MAX_VALUE, Double.MAX_VALUE),
        POSITIVE_DOUBLES(0.0, Double.MAX_VALUE),
        BYTES(Byte.MIN_VALUE, Byte.MAX_VALUE),
        POSITIVE_BYTES(0, Byte.MAX_VALUE),
        FLOATS(-Float.MAX_VALUE, Float.MAX_VALUE),
        POSITIVE_FLOATS(0f, Float.MAX_VALUE),;

        private final Supplier<Number[]> domainSupplier;

        Domain(Number... domain) {
            this.domainSupplier = () -> Arrays.copyOf(domain, domain.length, Number[].class);
        }

        private final Number[] getDomain() {
            return this.domainSupplier.get();
        }

    }

    private SocketHints() { /* no op */ }

    /**
     * These {@link SocketHint SocketHints} should only be used in {@link InputSocket InputSockets}
     */
    public static final class Inputs {
        private Inputs() { /* no op */ }

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
                                                                      final Number low, final Number high) {
            return createNumberSocketHintBuilder(identifier, number, new Number[]{low, high}).view(SocketHint.View.SLIDER).build();
        }

        public static SocketHint<Number> createNumberSliderSocketHint(final String identifier, final Number number,
                                                                      final Domain domain) {
            return createNumberSocketHintBuilder(identifier, number, domain.getDomain()).view(SocketHint.View.SLIDER).build();
        }

        public static SocketHint<Number> createNumberSpinnerSocketHint(final String identifier, final Number number,
                                                                       final Number low, final Number high) {
            return createNumberSocketHintBuilder(identifier, number, new Number[]{low, high}).view(SocketHint.View.SPINNER).build();
        }

        public static SocketHint<Number> createNumberSpinnerSocketHint(final String identifier, final Number number,
                                                                       final Domain domain) {
            return createNumberSocketHintBuilder(identifier, number, domain.getDomain()).view(SocketHint.View.SPINNER).build();
        }

        public static SocketHint<Number> createNumberSpinnerSocketHint(final String identifier, final Number number) {
            return createNumberSocketHintBuilder(identifier, number).view(SocketHint.View.SPINNER).build();
        }

        public static SocketHint<List> createNumberListRangeSocketHint(final String identifier, final Number low, final Number high) {
            return createNumberListSocketHintBuilder(identifier, new Number[]{low, high})
                    .view(SocketHint.View.RANGE)
                    .build();
        }

        public static SocketHint<List> createNumberListRangeSocketHint(final String identifier, final Domain domain) {
            return createNumberListSocketHintBuilder(identifier, domain.getDomain())
                    .view(SocketHint.View.RANGE)
                    .build();
        }
    }

    /**
     * These {@link SocketHint SocketHints} should only be used in {@link InputSocket InputSockets}
     */
    public static final class Outputs {
        private Outputs() { /* no op */ }

        public static SocketHint<Mat> createMatSocketHint(final String identifier) {
            return Inputs.createMatSocketHint(identifier, true);
        }

        public static SocketHint<Point> createPointSocketHint(final String identifier) {
            return Inputs.createPointSocketHint(identifier, true);
        }

        public static SocketHint<Size> createSizeSocketHint(final String identifier) {
            return Inputs.createSizeSocketHint(identifier, true);
        }

        public static SocketHint<Boolean> createBooleanSocketHint(final String identifier, boolean defaultValue) {
            return new SocketHint.Builder(Boolean.class).identifier(identifier).initialValue(defaultValue).publishable(true).build();
        }

        public static SocketHint<Number> createNumberSocketHint(final String identifier, Number defaultValue) {
            return new SocketHint.Builder(Number.class).identifier(identifier).initialValue(defaultValue).publishable(true).build();
        }
    }

    public static <T extends Enum<T>> SocketHint<T> createEnumSocketHint(final String identifier,
                                                                         final T defaultValue) {
        return new SocketHint.Builder(defaultValue.getClass())
                .identifier(identifier).initialValue(defaultValue)
                .view(SocketHint.View.SELECT)
                .domain(defaultValue.getClass().getEnumConstants()).build();
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



    /*\*** PRIVATE PARTIALLY CONSTRUCTED BUILDERS BELOW ***\*/

    private static <T> SocketHint.Builder<T> createObjectSocketHintBuilder(final String identifier,
                                                                           final Class<T> type,
                                                                           final Supplier<T> supplier,
                                                                           final boolean withDefault) {
        final SocketHint.Builder builder = new SocketHint.Builder(type).identifier(identifier);
        if (withDefault) return builder.initialValueSupplier(supplier);
        else return builder;
    }

    private static SocketHint.Builder<List> createNumberListSocketHintBuilder(final String identifier, final Number[] domain) {
        return new SocketHint.Builder(List.class).identifier(identifier)
                .initialValueSupplier(() -> new ArrayList(Arrays.asList(domain)))
                .domain(new List[]{Arrays.asList(domain)});
    }

    private static SocketHint.Builder<Number> createNumberSocketHintBuilder(final String identifier,
                                                                           final Number number,
                                                                           final Number[] domain) {
        return createNumberSocketHintBuilder(identifier, number).domain(domain);
    }

    private static SocketHint.Builder<Number> createNumberSocketHintBuilder(final String identifier,
                                                                            final Number number) {
        return new SocketHint.Builder(Number.class).identifier(identifier).initialValue(number);
    }
}
