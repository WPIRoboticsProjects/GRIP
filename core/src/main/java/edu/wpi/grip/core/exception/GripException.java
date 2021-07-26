
package edu.wpi.grip.core.exception;

/**
 * An exception thrown when something goes wrong with an internal GRIP
 * operation. This class is {@code abstract} to encourage making subclasses
 * for specific cases.
 */
public abstract class GripException extends RuntimeException {

  public GripException(String message) {
    super(message);
  }

  public GripException(String message, Throwable cause) {
    super(message, cause);
  }

}
