package edu.wpi.grip.core.observables;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Default thread-unsafe implementation of {@link Observable}. For a thread-safe solution, see
 * {@link SynchronizedObservable}.
 *
 * @param <T> the type of the values to observe
 */
public class SimpleObservable<T> implements Observable<T> {

  private final Set<Observer<? super T>> observers = new LinkedHashSet<>();
  private T value;

  /**
   * Creates an observable value with no initial value.
   */
  SimpleObservable() {
    this(null);
  }

  /**
   * Creates an observable value with the given initial value.
   *
   * @param initialValue the initial value
   */
  SimpleObservable(T initialValue) {
    this.value = initialValue;
  }

  @Override
  public void addObserver(Observer<? super T> observer) {
    Objects.requireNonNull(observer, "Listener cannot be null");
    observers.add(observer);
  }

  @Override
  public void removeObserver(Observer<? super T> observer) {
    observers.remove(observer);
  }

  @Override
  public T get() {
    return value;
  }

  @Override
  public void set(T value) {
    T previous = this.value;
    if (!Objects.equals(previous, value)) {
      this.value = value;
      observers.forEach(l -> l.onChange(previous, value));
    }
  }

}
