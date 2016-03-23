
package edu.wpi.grip.core.util;

/**
 * Simple thread-safe class to allow for setting values from lambdas.
 */
public final class Holder<T> {

    private T value;

    /**
     * Creates a new holder with the given default value.
     */
    public Holder(T value) {
        this.value = value;
    }

    /**
     * Gets the value of this holder. This is a thread-safe operation.
     *
     * @return the value of this holder.
     */
    public synchronized T get() {
        return value;
    }

    /**
     * Sets the value of this holder. This is a thread-safe operation.
     *
     * @param value the new value of this holder.
     */
    public synchronized void set(T value) {
        this.value = value;
    }

}
