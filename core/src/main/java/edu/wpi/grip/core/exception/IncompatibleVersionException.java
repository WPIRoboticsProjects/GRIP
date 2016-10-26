package edu.wpi.grip.core.exception;

/**
 * An exception thrown when trying to load a saved project created in an incompatible version
 * of GRIP.
 */
public class IncompatibleVersionException extends InvalidSaveException {

  public IncompatibleVersionException(String message) {
    super(message);
  }

  public IncompatibleVersionException(String message, Throwable cause) {
    super(message, cause);
  }

}
