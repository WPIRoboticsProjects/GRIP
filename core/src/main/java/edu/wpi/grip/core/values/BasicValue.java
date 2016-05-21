package edu.wpi.grip.core.values;

/**
 * A basic {@link Value} that just holds a a value
 * @param <T>
 */
public class BasicValue<T> extends ValueImpl<T> {

    public BasicValue(T value) {
        super(value);
    }
}
