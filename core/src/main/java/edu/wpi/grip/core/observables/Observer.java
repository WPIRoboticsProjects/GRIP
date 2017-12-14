package edu.wpi.grip.core.observables;

/**
 * Observes changes to the value of an {@link Observable}.
 *
 * @param <T> the type of the value to observe
 */
@FunctionalInterface
public interface Observer<T> {

  /**
   * Called when the value of the observable changes.
   *
   * @param previous the previous value of the observable
   * @param current  the current value of the observable
   */
  void onChange(T previous, T current);

}
