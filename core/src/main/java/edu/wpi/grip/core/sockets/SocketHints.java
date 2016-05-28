package edu.wpi.grip.core.sockets;


import com.google.common.reflect.TypeToken;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Commonly used {@link SocketHint SocketHints}
 */
public final class SocketHints {

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

        public static SocketHint<Point> createPointSocketHint(final String identifier, int x, int y) {
            return createObjectSocketHintBuilder(identifier, Point.class, () -> new Point(x, y), true).build();
        }

        public static SocketHint<Number> createNumberSliderSocketHint(final String identifier, final Number number,
                                                                      final Number low, final Number high) {
            return createNumberSocketHintBuilder(identifier, number, new Number[]{low, high}).view(SocketHint.View.SLIDER).build();
        }

        public static SocketHint<Number> createNumberSpinnerSocketHint(final String identifier, final Number number,
                                                                       final Number low, final Number high) {
            return createNumberSocketHintBuilder(identifier, number, new Number[]{low, high}).view(SocketHint.View.TEXT).build();
        }

        public static SocketHint<Number> createNumberSpinnerSocketHint(final String identifier, final Number number) {
            return createNumberSocketHintBuilder(identifier, number).view(SocketHint.View.TEXT).build();
        }

        public static SocketHint<List<Number>> createNumberListRangeSocketHint(final String identifier, final Number low, final Number high) {
            return createNumberListSocketHintBuilder(identifier, new Number[]{low, high})
                    .view(SocketHint.View.RANGE)
                    .build();
        }

        public static SocketHint<String> createTextSocketHint(final String identifier, final String str) {
            return new SocketHint.Builder<>(String.class)
                    .identifier(identifier)
                    .initialValue(str)
                    .view(SocketHint.View.TEXT)
                    .build();
        }

        public static SocketHint<Boolean> createCheckboxSocketHint(final String identifier, final Boolean initialValue) {
            return new SocketHint.Builder<>(Boolean.class)
                    .identifier(identifier)
                    .initialValue(initialValue)
                    .view(SocketHint.View.CHECKBOX)
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
            return new SocketHint.Builder<>(Boolean.class).identifier(identifier).initialValue(defaultValue).build();
        }

        public static SocketHint<Number> createNumberSocketHint(final String identifier, Number defaultValue) {
            return createNumberSocketHintBuilder(identifier, defaultValue).build();
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
        return new SocketHint.Builder<>(Boolean.class)
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
        final SocketHint.Builder<T> builder = new SocketHint.Builder<>(type).identifier(identifier);
        if (withDefault) return builder.initialValueSupplier(supplier);
        else return builder;
    }

    @SuppressWarnings("unchecked")
    private static SocketHint.Builder<List<Number>> createNumberListSocketHintBuilder(final String identifier, final Number[] domain) {
        return new SocketHint.Builder<>((Class<List<Number>>) new TypeToken<List<Number>>(){}.getRawType()).identifier(identifier)
                .initialValueSupplier(() -> new ArrayList<>(Arrays.asList(domain)))
                .domain(new List[]{Arrays.asList(domain)});
    }

    private static SocketHint.Builder<Number> createNumberSocketHintBuilder(final String identifier,
                                                                            final Number number,
                                                                            final Number[] domain) {
        return createNumberSocketHintBuilder(identifier, number).domain(domain);
    }

    private static SocketHint.Builder<Number> createNumberSocketHintBuilder(final String identifier,
                                                                            final Number number) {
        return new SocketHint.Builder<>(Number.class).identifier(identifier).initialValue(checkNotNull(number, "Number can not be null"));
    }
}
