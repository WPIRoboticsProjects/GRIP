package edu.wpi.grip.core.values;


import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Represents an empty {@link Value}
 * @param <T> The type of empty value this object represents
 */
public class EmptyValue<T> implements Value<T> {
    @Override
    public T get() {
        throw new NoSuchElementException("No value present");
    }

    @Override
    public void ifPresent(Consumer<? super T> consumer) {
        /* no-op */
    }

    @Override
    public T orElse(T other) {
        return other;
    }

    @Override
    public boolean isPresent() {
        return false;
    }
}
