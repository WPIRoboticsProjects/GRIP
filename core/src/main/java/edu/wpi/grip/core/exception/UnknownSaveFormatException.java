package edu.wpi.grip.core.exception;

/**
 * An exception thrown when loading a save file that can't be deserialized to a known type.
 */
public class UnknownSaveFormatException extends InvalidSaveException {

  public UnknownSaveFormatException(String message) {
    super(message);
  }

}
