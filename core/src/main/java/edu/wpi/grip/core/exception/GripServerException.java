package edu.wpi.grip.core.exception;

/**
 * An exception thrown when something goes wrong in the
 * {@link edu.wpi.grip.core.http.GripServer GripServer}.
 */
public class GripServerException extends GripException {

  public GripServerException(String message) {
    super(message);
  }

  public GripServerException(String message, Throwable cause) {
    super(message, cause);
  }
}
