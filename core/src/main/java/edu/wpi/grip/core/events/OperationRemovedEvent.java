package edu.wpi.grip.core.events;

import edu.wpi.grip.core.OperationDescription;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event fired when an operation is removed from the palette.
 */
public class OperationRemovedEvent {

  private final OperationDescription removedOperation;

  public OperationRemovedEvent(OperationDescription removedOperation) {
    this.removedOperation = checkNotNull(removedOperation);
  }

  public OperationDescription getRemovedOperation() {
    return removedOperation;
  }
}
