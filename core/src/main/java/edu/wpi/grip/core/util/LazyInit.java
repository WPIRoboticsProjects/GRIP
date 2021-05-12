package edu.wpi.grip.core.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A holder for data that gets lazily initialized.
 *
 * @param <T> the type of held data
 */
public class LazyInit<T> {

  private T value = null;
  private final Supplier<? extends T> factory;

  /**
   * Creates a new lazily initialized data holder.
   *
   * @param factory the factory to use to create the held value
   */
  public LazyInit(Supplier<? extends T> factory) {
    this.factory = Objects.requireNonNull(factory, "factory");
  }

  /**
   * Gets the value, initializing it if it has not yet been created.
   *
   * @return the held value
   */
  public T get() {
    if (value == null) {
      value = factory.get();
    }
    return value;
  }

  /**
   * Clears the held value. The next call to {@link #get()} will re-instantiate the held value.
   */
  public void clear() {
    value = null;
  }

}
