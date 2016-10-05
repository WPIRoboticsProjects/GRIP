package edu.wpi.grip.core.operations;

import edu.wpi.grip.core.OperationMetaData;

import com.google.common.collect.ImmutableList;

/**
 * Contains operations within itself.
 */
public interface Operations {
  /**
   *
   * @return All operations within.
   */
  ImmutableList<OperationMetaData> operations();
}
