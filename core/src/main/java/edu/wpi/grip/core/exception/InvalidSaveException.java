package edu.wpi.grip.core.exception;

/**
 * An exception thrown when trying to load an invalid saved project.
 */
public class InvalidSaveException extends GripException {

  public InvalidSaveException(String message) {
    super(message);
  }

  public InvalidSaveException(String message, Throwable cause) {
    super(message, cause);
  }

}
