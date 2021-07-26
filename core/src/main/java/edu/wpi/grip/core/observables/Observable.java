package edu.wpi.grip.core.observables;

/**
 * An observable wraps a value that, when changed, will notify listeners of the change.
 *
 * @param <T> the type of value to observe
 */
public interface Observable<T> {

  /**
   * Add an observer to this observable. It will be notified of any future changes to the value of
   * this observable. Listeners will be fired in the order in which they were added, and on the
   * thread that updates the observed value. Because of this, listeners should take as little time
   * as possible to run (unless submitting a long-running task to a worker thread).
   */
  void addObserver(Observer<? super T> observer);

  /**
   * Removes an observer from this observable. The observer will not be updated with future changes
   * to the value of this observable.
   */
  void removeObserver(Observer<? super T> observer);

  /**
   * Gets the current value of this observable.
   */
  T get();

  /**
   * Sets the value of this observable. If it's not {@link Object#equals(Object) equal} to the
   * current value, all observers will be notified of the change.
   */
  void set(T value);

  /**
   * Creates an observable initialized to the given value. This observable is <i>not</i>
   * thread-safe; for a thread-safe observable, use {@link #synchronizedOf(Object) synchronizedOf}.
   *
   * @param value the initial value of the observable
   * @param <T>   the type of value in the observable
   */
  static <T> Observable<T> of(T value) {
    return new SimpleObservable<>(value);
  }

  /**
   * Creates a thread-safe observable initialized to the given value.
   *
   * @param value the initial value of the observable
   * @param <T>   the type of value in the observable
   */
  static <T> Observable<T> synchronizedOf(T value) {
    return new SynchronizedObservable<>(value);
  }

}
