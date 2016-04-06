package edu.wpi.grip.core.values;


import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A concrete implementation of {@link Value} that holds an actual value
 * @param <T> The type that this Value holds
 */
public abstract class ValueImpl<T> implements Value<T> {

    private final T value;
    public ValueImpl(T value) {
        this.value = checkNotNull(value, "Value cannot be null");
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void ifPresent(Consumer<? super T> consumer) {
        consumer.accept(value);
    }

    @Override
    public T orElse(T other) {
        return value;
    }

    @Override
    public boolean isPresent() {
        return true;
    }
}
