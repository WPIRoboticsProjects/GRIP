package edu.wpi.grip.core;

import java.util.Comparator;

/**
 * An interface for getting the indices of steps.
 */
public interface StepIndexer extends Comparator<Step> {

  /**
   * Gets the index of the given step.
   *
   * @param step the step to get the index of
   * @return the index of the given step, or -1 if this object does not contain that step
   */
  int indexOf(Step step);

  /**
   * Compares two steps based on their indexes. <i>This is not consistent with {@code equals()}</i>.
   *
   * @param o1 the first step to compare
   * @param o2 the second step to compare
   */
  @Override
  default int compare(Step o1, Step o2) {
    return indexOf(o1) - indexOf(o2);
  }

}
