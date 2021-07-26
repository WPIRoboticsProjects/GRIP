package edu.wpi.grip.core.observables;

/**
 * A thread-safe implementation of {@link Observable}. This should only be used in situations where
 * observers are <i>very likely</i> to run in multiple threads. Otherwise, {@link SimpleObservable}
 * should be used to avoid the penalties of {@code synchronized} and lock contention.
 *
 * @param <T> the type of the values to observe
 */
public class SynchronizedObservable<T> extends SimpleObservable<T> {

  private final Object observersLock = new Object();
  private final Object valueLock = new Object();

  SynchronizedObservable() {
    super();
  }

  SynchronizedObservable(T initialValue) {
    super(initialValue);
  }

  @Override
  public void addObserver(Observer<? super T> observer) {
    synchronized (observersLock) {
      super.addObserver(observer);
    }
  }

  @Override
  public void removeObserver(Observer<? super T> observer) {
    synchronized (observersLock) {
      super.addObserver(observer);
    }
  }

  @Override
  public T get() {
    synchronized (valueLock) {
      return super.get();
    }
  }

  @Override
  public void set(T value) {
    synchronized (valueLock) {
      synchronized (observersLock) {
        super.set(value);
      }
    }
  }

}
