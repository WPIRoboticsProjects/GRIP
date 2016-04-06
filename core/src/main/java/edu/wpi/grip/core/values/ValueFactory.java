package edu.wpi.grip.core.values;


import javax.annotation.Nullable;
import java.util.function.Function;

public class ValueFactory <T> {
    private static final Value EMPTY = new EmptyValue<>();
    private final Function<T, ? extends Value> valueConstructor;

    ValueFactory(Function<T, ? extends Value> valueConstructor) {
        this.valueConstructor = valueConstructor;
    }

    public Value<T> create(@Nullable T value) {
        if (value == null) {
            return EMPTY;
        } else {
            return valueConstructor.apply(value);
        }
    }

    public static <T> ValueFactory<T> createFactory(Class<T> factoryFor) {
        return new ValueFactory<>(BasicValue::new);
    }
}
